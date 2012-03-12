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
	private Condition nI;
	private boolean ini;
	private int waintingWriter;

	public boolean isINI(){
		return this.ini;
	}
	public void setINI(){	
		this.ini = true;
	}
	public enum State{{
		NI;
		NL;
		RLT;
		WLT;
		RLC;
		WLC;
		RLT_WLC;
	}

	public SharedObject(int id,Object object,Client c){
		this.id = id;
		this.obj = object;
		this.lockState = NI;
	this.client = c;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.available = this.lock.newCondition();
	}

	public synchronized void updateLock(State verrou){
		switch(verrou){
			case NI:
				this.lockState = NI; 
			break;
			case NL:
				this.lockState = NL;
			break;
			case RLC:
				this.lockState = RLC;
			break;
			case WLC:
				this.lockState = WLC;
			break;
			case RLT:
				this.lockState = RLT;
			break;
			case RLT_WLC:
				this.lockState = RLT_WLC;
			break;
		}
	}
	public synchronized void signalINI(){
		this.nI.signal();
	}
	
	public synchronized void awaitINI() throws InterruptedException{
		this.nI.await();
	}
	// invoked by the user program on the client node
	public void lock_read() {
		switch(this.lockState){
			case RLC :
				this.updateLock(RLT);	
			break;
			case WLC:
				this.updateLock(RLT_WLC);
			break;
			default:
				this.obj = client.lock_read(this.id);
				this.lockState=RLT;
			break;					
		}
		
	}

	// invoked by the user program on the client node
	public void lock_write() {
		switch(this.lockState){
		
			case WLC:
				client.lock_write(this.id);
				this.lockState=WLT;
			
			break;
			default: 
				if(this.waitingWriter==0){
					this.obj =  client.lock_write(this.id);
					this.lockState=WLT;
				}else{
					this.lockState=NL;
	 				this.available.signal();
					this.lock_write();
				}
			break;
		}
	}

	public synchronized void lock(){
		this.lock.lock();
	}
	public synchronized void unlockLock(){
		this.lock.unlock();
	}
	

	// invoked by the user program on the client node
	public synchronized void unlock() {
		switch lockState{
			case RLT:
			lockState = RLC;
			break;
			case WLT:
			lockState = WLC;
			case RLT_WLC:
			lockState = WLC;
		}
		this.available.signal();
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		this.lock.lock();
		if(this.lockState==WLT){
			while(this.lockState==WLT){
				this.available.await();
			}
			this.lockState=RLC;
		}
		if(this.lockState==RLT_WLC){
			this.lockState=RLT;
		}
		if(this.lockState==WLC){
			this.lockState=RLC;
		}
		this.lock.unlock();
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		this.lock.lock();
		while(this.lockState==RLT){
			try{
				this.waitingWriter+=1;
				this.available.await();
				this.waitingWriter-=1;
			}catch(InterruptedException r){}
		}
		this.updateLock(NL);
		this.lock.lock();
	}

	public synchronized Object invalidate_writer() {
		this.lock.lock();	
		while(this.lockState==WLT){
			try{
				this.available.await();
			}catch(InterruptedException t){}
		}
		this.lockState=NL;
		this.unlock();	
		return obj;
				
	}

	public int getID(){
		return id;
	}
}
