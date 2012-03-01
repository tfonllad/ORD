/**@version etape1
**/

public class ServerObject{

	private int lockState;
	private int id;
	private ArrayList<Client_itf> clientList; // List of client who have up-to-date SharedObject 
	private Lock lock;
	private Condition nI;			
	/** Constructor ServerObject
	**/
	public ServerObject(int id,int lock){
		this.id = id;	
		this.lockState = lock;
		this.lock = new ReentrantLock();
		this.nI = this.lock.newCondition();
	}
	public void awaitNI() throws InterruptedException{
		this.nI.await();
	}
	public void signalNI(){
		this.nI.signal();
	}
	public void lockNI(){
		this.lock.lock();
	}
	public void unlockNI(){
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
