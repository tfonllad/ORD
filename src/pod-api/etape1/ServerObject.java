/**@version etape1
**/
import java.util.ArrayList;
import java.util.concurrent.locks.*;
import java.rmi.*;
public class ServerObject{

	//identification	
	private int id;
	private ArrayList<Client_itf> readerList; 
	private Client_itf writer;	
	public Object obj;		
	
	//State
	private State lockState;
	public enum State{
		NL,
		RL,
		WL;	
	}
	
	//Consistency
	private ReentrantLock lock;

	
	/** Constructor ServerObject
	*@param id : the unique id.
	*@param o : cached object. Used for lookup and reader without writer
	*invalidation
	**/
	public ServerObject(int id,Object o){
		this.id = id;	
		this.obj = o;
		this.lock = new ReentrantLock();
	}
	public synchronized void lock(){
		this.lock.lock();
	}
	public synchronized void unlock(){
		this.lock.unlock();
	}
	/** Methode updateLock is called after waiting process get out the await
 	* loop.
	* @param verrou
	* @return void
	**/
	public void updateLock(State verrou){
		switch(verrou){
			case NI:
				this.lockState = NI;
			break;
			case NL:
				this.lockState = NL;
			break;
			case RL:
				this.lockState = RL;
			break;
			case WL:
				this.lockState = WL;
			break;
		}
	}

	public int getID(){
		return this.id;
	}

	/** method invalidate_write: used to get the last up-to-date object
	 *from a writer once he unlocked it. If they are no current writer, return the
 	 *cached object. Also set writer to null;
	 *@return obj 
	 **/
	public Object invalidate_writer(){
		Client c;
		if(writer!=null){
			obj = writer.invalidate_writer(this.id);
			writer = null;
		}
		return obj;
	}
	
	/** Method invalidate_reader : used to wait for reader to unlock.
	**/
	public void invalidate_reader(){
		for(Client_itf cli : readerList){
			try{
				cli.invalidate_reader(this.id);		
				this.readerList.remove(cli);
			}catch(RemoteException r){
				r.prinStackTrace();
			}
		}
	}	
	/**Method reduce_lock : similar to invalidate_writer except the writer
	 * becomes a reader
         *@return o : up-to-date object
	**/
	public Object reduce_lock(){
		Client c;
		if(writer!=null){
			try{
				obj = writer.reduce_lock(this.id);
				this.readerList.add(this.writer);
				writer = null;
			}catch(RemoteException r){
				r.printStackTrace();
			}finally{
			 	writer=null; //Si on perd la connexion vers
					//l'écrivain, on le dégage et on renvoit le dernier objet du cache
			}
		}
	return obj;	
	}
	
	/**Method lock_read : called by client to get lock on the object.The
 	* method call reduce_lock on the writer if not null
	* @return o : up-to-date object
	**/
	public Object lock_read(Client c){
		while(lockState==WL){
			obj = this.reduce_lock();
		}
		lockState=RL;
		return obj;
	}	
	/**Method lock_writer : similar to lock_write, invalidate both writer
 	* and readers.
	* @return obj : up-to-date object
	**/
	public Object lock_write(Client c){
		while(lockState==WL||this.readerList.size()!=0){
				obj = this.invalidate_writer();	
				this.invalidate_reader();
		}
		this.lockState = WL;
		writer = c;
		return obj;
	}
}
