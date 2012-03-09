/**@version etape1
**/
import java.util.ArrayList;
import java.util.concurrent.locks.*;

public class ServerObject{

	private State lockState;
	private int id;
	private ArrayList<Client_itf> clientList; // List of client who have up-to-date SharedObject 
	private ReentrantLock lock;
	private Condition nI;
	private Condition releaseLock; //either unlock or reduce_lock or invalidate
	/** Constructor ServerObject
	**/
	
	public ServerObject(int id){
		this.id = id;	
		this.lockState = State.NI;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.releaseLock() = this.lock.newCondition();
	}
	public synchronized void awaitINI() throws InterruptedException{
		this.nI.await();
	}
	public synchronized void signalINI(){
		this.nI.signal();
	}
	public synchronized void releaseLock(){
		this.releaseLock.signal();
	}
	public synchronized void takeLock(){
		this.releaseLock.await();
	}

	/** Methode updateLock is called after waiting process get out the await
 	* loop.
	* @param verrou
	* @return void
	**/
	public void updateLock(State verrou){
		switch(verrou){
			case NI:
				this.lockState = State.NI;
			break;
			case NL:
				this.lockState = State.NL;
			break;
			case RL:
				this.lockState = State.RL;
			break;
			case WL:
				this.lockState = State.WL;
			break;
		}
	}
	public synchronized void lock(){
		this.lock.lock();
	}
	public synchronized void unlock(){
		this.lock.unlock();
	}
	public int getID(){
		return this.id;
	}
	State getLockState(){
		return this.lockState;
	}
	public Object synchronized lock_read(Client_itf client){	
	}
	public Object synchronized lock_write(Client_itf client){
	} 
}	
