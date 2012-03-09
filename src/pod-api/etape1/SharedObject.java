import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id;
	public Object obj;
	private State lockState;
	private ReentrantLock lock;
	private Condition releaseLock;
	private Client client;
	
	public SharedObject(int id,Object object){
		this.id = id;
		this.obj = object;
		this.lockState = State.NI;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.releaseLock = this.lock.newCondition();
	}
	
	public void updateLock(State verrou){
		switch(verrou){
			case NI:
				this.lockState = State.NI; 
			break;
			case NL:
				this.lockState = State.NL;
			break;
			case RLC:
				this.lockState = State.RLC;
			break;
			case WLC:
				this.lockState = State.WLC;
			break;
			case RLT:
				this.lockState = State.RLT;
			break;
			case RLT_WLC:
				this.lockState = State.RLT_WLC;
			break;
		}
	}
	public synchronized void signalINI(){
		this.nI.signal();
		}
	}
	public synchronized void awaitINI() throws InterruptedException{
		this.nI.await();
		}
	}
	// invoked by the user program on the client node
	public void lock_read() {
		// si on est en locket cached, inutile d'appeler le serveur.
		
		switch(this.lockState){
			case RLC :
			//this.lock.lock();
			this.updateLock(State.RLT);
			//call server informer de la maj du lock
			
			break;

			case WLC:
			//this.lock.lock();
			this.updateLock(State.RLT_WLC);
			//call server : informer de la maj.
			break;
			
			default:
				client.lock_read(this.id);
				this.lock.lock();// a vérifier
				this.lockState=State.RLT;
			break;					
		}
	}

	// invoked by the user program on the client node
	public void lock_write() {

	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		if(this.lockState==State.WLT){
			while(this.lockState==State.WLT){
				this.releaseLock.await();
			}
			this.lockState=State.RLC;
		}
		if(this.lockState==State.RLT_WLC){
			this.lockState=RLT;
		}
		if(this.lockState==WLC){
			this.lockState==RLC;
		}

		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
	 	
	}

	public synchronized Object invalidate_writer() {
		
		while(this.lockState==State.WLT){
			this.releaseLock.await();
		}
		this.lockState=State.NL;	
		return obj;
				
	}

	public int getID(){
		return id;
	}
		
}
