/**@version etape1
**/
import java.util.ArrayList;
import java.util.concurrent.locks.*;

public class ServerObject{

	private LockS lockState;
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
	public ServerObject(int id,int lock){
		this.id = id;	
		this.lockState = lock;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
		this.nL = this.lock.newCondition();
		this.rLC = this.lock.newCondition();
		this.wLC = this.lock.newCondition();
		this.rLT = this.lock.newCondition();
		this.rLT_WLC = this.lock.newCondition();
	}
	//Called when process can't meet requirement
	public void await(LockS verrou) throws InterruptedException{
		switch(verrou){
			case LockS.NI:
				this.nI.await();
			break;
			case LockS.NL:
				this.nL.await();
			break;
			case LockS.RLC:
				this.rLC.await();
			break;
			case LockS.WLC:
				this.wLC.await();
			break;
			case LockS.RLT:
				this.rLT.await();
			break;
			case Locks.RLT_WLC:
				this.rLT_WLC.await();
			break;
		}
	}
	/**method signal is called once requirement are meet
	*@param verrou lock to release waiting process. This process will gain
	*acces to the object with a certain state (LockState), allowing him
	*certains rights.
	*@return void
	**/
	public void signal(LockS verrou ){
		switch(verrou){
			case LockS.NI:
				this.nI.signal();
				this.lockState = LockS.NI; 
			break;
			case LockS.NL:
				this.nL.signal();
				this.lockState = LockS.NL;
			break;
			case LockS.RLC:
				this.rLC.signal();
				this.lockState = LockS.RLC;
			break;
			case LockS.WLC:
				this.wLC.signal();
				this.lockState = LockS.WLC;
			break;
			case LockS.RLT:
				this.rLT.signal();
				this.lockState = LockS.RLT;
			break;
			case Locks.RLT_WLC:
				this.rLT_WLC.signal();
				this.lockState = LockS.RLT_WLC;
			break;
		}
	}
	public void lock(){
		this.lock.lock();
	}
	public void unlock(){
		this.lock.unlock();
	}
	public int getID(){
		return this.id;
	}
	int getLockState(){
		return this.lockState;
	}
	void setLockState(int i){
		this.lockState = i;
	}
	public Client_itf getClient(){
		return this.clientList.get(0);
	}
	public void addClient(Client_itf c){
		this.clientList.add(c);
	}
}
