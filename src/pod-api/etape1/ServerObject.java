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
	public synchronized void releaseLock(){
		this.releaseLock.signal();
	}
	public synchronized void takeLock(){
		this.releaseLock.await();
	}
	public ServerObject(int id){
		this.id = id;	
		this.lockState = State.NI;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.releaseLock() = this.lock.newCondition();
	}
	//Called when process can't meet requirement
	/** method await : block the process on one condition		
	*@param : verrou, the condition
	**/

	public void await(State verrou) throws InterruptedException{
		switch(verrou){
			case NI:
				this.nI.await();
			break;
			case NL:
				this.nL.await();
			break;
			case RL:
				this.rL.await();
			break;
			case WL:
				this.wL.await();
			break;
		}
	}
	/**method signal is called once requirement are meet
	*@param verrou lock to release waiting process. If process get out of
	*the loop it will call updateLock(verrou) to update the lock
	*@return void
	**/
	public void signal(State verrou){
		switch(verrou){
			case NI:
				this.nI.signal();
			break;
		}
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
}	
