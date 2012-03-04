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
	private Condition nL;
	private Condition rLC;	
	private Condition wLC;
	private Condition rLT;
	private Condition rLT_WLC;

	/** Constructor ServerObject
	**/
	public ServerObject(int id){
		this.id = id;	
		this.lockState = State.NI;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.nL = this.lock.newCondition();
		this.rLC = this.lock.newCondition();
		this.wLC = this.lock.newCondition();
		this.rLT = this.lock.newCondition();
		this.rLT_WLC = this.lock.newCondition();
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
			case RLC:
				this.rLC.await();
			break;
			case WLC:
				this.wLC.await();
			break;
			case RLT:
				this.rLT.await();
			break;
			case RLT_WLC:
				this.rLT_WLC.await();
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
			case NL:
				this.nL.signal();
			break;
			case RLC:
				this.rLC.signal();
			break;
			case WLC:
				this.wLC.signal();
			break;
			case RLT:
				this.rLT.signal();
			break;
			case RLT_WLC:
				this.rLT_WLC.signal();
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
	public Client getClient(){
		return this.clientList.get(0);
	}
	public void addClient(Client_itf c){
		this.clientList.add(c);
	}
}
