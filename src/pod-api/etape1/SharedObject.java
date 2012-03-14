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
				System.out.println("Pre : RLC");
				this.lockState=State.RLT;
				System.out.println("Post:RLT");
			break;
			case WLC:
				System.out.println("Pre : WLC");
				this.lockState=State.RLT_WLC;
				System.out.println("Post : RLT_WCL");
			break;
			default:
				this.obj = client.lock_read(this.id);
				this.lockState=State.RLT;
			break;					
		}
		
	}

	// invoked by the user program on the client node
	public void lock_write() {
		switch(this.lockState){
			case WLC:
				System.out.println("Pre : WLC");	
				this.lockState=State.WLT;
				System.out.println("Post : WLT");	
			break;
			default: 
				if(this.waitingWriter==0){
					System.out.println("client.lock_write");	
					this.obj =  client.lock_write(this.id);
					System.out.println("Client done lock_write");
					this.lockState=State.WLT;
					System.out.println("Post = WLT");
				}else{
					this.lockState=State.NL;
					System.out.println("State = NL");
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
			System.out.println("unlock = RLC");
			break;
			case WLT:
			lockState = State.WLC;
			System.out.println("unlock = WLC");
			case RLT_WLC:
			lockState = State.WLC;
			System.out.println("State = WLC");
		}
		this.available.signal();	
		this.lock.unlock();
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		System.out.println("Propagationde reduce_lock : SharedObject");
		this.lock.lock();
		System.out.println("le SharedObject est donc locked");
		if(this.lockState==State.WLT){
			while(this.lockState==State.WLT){
				try{
					System.out.println("await");
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
		this.lock.unlock();
		System.out.println("Le SharedObject n'est plus locked");
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		this.lock.lock();
		while(this.lockState==State.RLT){
			try{
				this.waitingWriter+=1;
				System.out.println("ReaderAwait");
				this.available.await();
				this.waitingWriter-=1;
			}catch(InterruptedException r){}
		}
		this.lockState=State.NL;
		this.lock.unlock();
	}

	public synchronized Object invalidate_writer() {
		this.lock.lock();
		System.out.println("Shared : IW");	
		while(this.lockState==State.WLT){
			try{
				System.out.println("await");
				this.available.await();
			}catch(InterruptedException t){}
		}
		this.lockState=State.NL;
		this.lock.unlock();	
		return obj;
				
	}

	public int getID(){
		return id;
	}
}
