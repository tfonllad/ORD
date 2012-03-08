import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	

	//attribut lock

	private int id;
	public Object obj;
	private State lockState;
	private ReentrantLock lock;
	private Condition releaseLock;

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
	public void signal(State verrou){
		switch(verrou){
			case NI:
				this.nI.signal();
			break;

		}
	}
	public void await(State verrou) throws InterruptedException{
		switch(verrou){
			case NI:
				this.nI.await();
			break;
		}
	}
	// invoked by the user program on the client node
	public void lock_read() {
		//appel de 
	}

	// invoked by the user program on the client node
	public void lock_write() {
		//appel d
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
		

