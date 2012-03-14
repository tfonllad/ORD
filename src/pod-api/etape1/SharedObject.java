import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id;
	public Object obj;		
	private State lockState;
	private ReentrantLock lock;
	private Condition available;
	private Client client;
	private int waitingWriter;

	public enum State{	
		NL,
		RLT,
		WLT,
		RLC,
		WLC,
		RLT_WLC;
	}

	public SharedObject(int id,Object object,Client c){
		this.id = id;
		this.obj = object;
		this.lockState = State.NL;
		this.client = c;
		this.waitingWriter = 0;
		this.lock = new ReentrantLock();
		this.available = this.lock.newCondition();
	}

	// invoked by the user program on the client node
	public void lock_read() {
		
		switch(this.lockState){
			case RLC :
				System.out.println("OBTAINED from cache");
				this.lockState=State.RLT;
			break;
			case WLC:
				System.out.println("OBTAINED from cache");
				this.lockState=State.RLT_WLC;
			break;
			default:System.out.println("REQUEST lock_read");
				this.obj = client.lock_read(this.id);
				this.lockState=State.RLT;
				System.out.println("OBTAINED lock_read");
			break;					
		}
		
	}

	// invoked by the user program on the client node
	public void lock_write() {
		
		switch(this.lockState){
			case WLC:
				System.out.println("OBTAINED from cache");	
				this.lockState=State.WLT;
			break;
			default: 
				System.out.println("REQUEST lock_write");
				if(this.waitingWriter==0){	
					this.obj =  client.lock_write(this.id);
					this.lockState=State.WLT;
					System.out.println("OBTAINED lock_write");
				}else{
					this.lockState=State.NL;
					System.out.println("Je laisse la place");
	 				this.available.signal();
					this.lock_write();
				}
			break;
		}
	}

	// invoked by the user program on the client node
	public void unlock() {
		this.lock.lock();
		switch(this.lockState){
			case RLT:
			lockState = State.RLC;
			break;
			case WLT:
			lockState = State.WLC;
			case RLT_WLC:
			lockState = State.WLC;
		}
		this.available.signal();	
		this.lock.unlock();
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		this.lock.lock();
		System.out.println("demande de reduce_lock()");
		if(this.lockState==State.WLT){
			while(this.lockState==State.WLT){
				try{
					this.available.await();
				}catch(InterruptedException i){}
			}
			this.lockState=State.RLC;
		}
		if(this.lockState==State.RLT_WLC){
			this.lockState=State.RLT;
		}
		if(this.lockState==State.WLC){
			this.lockState=State.RLC;
		}
		System.out.println("fin de reduce_lock");
		this.lock.unlock();
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		this.lock.lock();
		System.out.println("demande invalidate_reader");
		while(this.lockState==State.RLT){
			try{
				this.waitingWriter+=1;
				System.out.println("Write++");
				this.available.await();
				System.out.println("Writer--");
				this.waitingWriter-=1;
			}catch(InterruptedException r){}
		}
		this.lockState=State.NL;
		System.out.println("fin d'invalidate_reader");
		this.lock.unlock();
	}

	public synchronized Object invalidate_writer() {
		this.lock.lock();
		System.out.println("fin d'invalidate_write");
		while(this.lockState==State.WLT){
			try{
				this.available.await();
			}catch(InterruptedException t){}
		}
		this.lockState=State.NL;
		System.out.println("fin d'invalidate_reader");
		this.lock.unlock();	
		return obj;
				
	}

	public int getID(){
		return id;
	}
}
