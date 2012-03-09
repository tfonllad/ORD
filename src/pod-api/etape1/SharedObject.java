import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id;
	public Object obj;
	private State lockState;
	private ReentrantLock lock;
	private Condition releaseLock;
	private Client client;

	public synchronized void releaseLock(){
		this.releaseLock.signal();
	}
	public synchronized void takeLock(){
		this.releaseLock.await();
	}

	public SharedObject(int id,Object object){
		this.id = id;
		this.obj = object;
		this.lockState = State.NI;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.releaseLock = this.lock.newCondition();
	}
	
	public synchronized boolean isWritable(){
		return this.getLockState().equals(State.NI)||this.getLockState().equals(State.WLT)||this.getLockState().equals(State.RLT_WLC);
	}
	public synchronized boolean isReadable(){
		return this.getLockState().equals(State.RLT)||this.getLockState().equals(State.WLT)||this.getLockState().equals(RLT_WLC)||this.getLockState().equals(State.NI)
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
	
	public synchronized void releaseLock(){
		this.releaseLock.signal();
	}
	
	public synchronized void takeLock() throws InterruptedException{
		this.releaseLock.await();
	}

	// invoked by the user program on the client node
	public void lock_read() {
		// si on est en locket cached, inutile d'appeler le serveur.
		
		switch(this.lockState){
			case RLC :
			this.lock.lock();
			this.updateLock(State.RLT);
			//call server informer de la maj du lock
			break;

			case WLC:
			this.lock.lock();
			this.updateLock(State.RLT_WLC);
			//call server : informer de la maj.
			break;
			
				
		}
		
		
	}

	// invoked by the user program on the client node
	public void lock_write() {
		this.client.lock_write(this.id);
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		return null;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {

	}

	public synchronized Object invalidate_writer() {
		return null;
	}

	public int getID(){
		return id;
	}
		
}
