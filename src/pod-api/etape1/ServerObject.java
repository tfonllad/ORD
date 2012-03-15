/**@version etape1
**/
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.rmi.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;


public class ServerObject{

	//identification	
	private int id;
	private List<Client_itf> readerList; 
	private Client_itf writer;	
	public Object obj;		
	private static Logger logger;	
	//State
	private State lockState;
	public enum State{
		NL,
		RL,
		WL;	
	}
	
	/**Constructor ServerObject
	*@param id : the unique id.
	*@param o : cached object. Used for lookup and reader without writer
	*invalidation
	**/
	public ServerObject(int id,Object o){
		this.id = id;	
		this.obj = o;
		this.readerList = new CopyOnWriteArrayList();
		logger = new Logger();
	}

	public int getID(){
		return this.id;
	}

	/**Method lock_read : called by client to get lock on the object.The
 	* method call reduce_lock on the writer if not null
	* @return o : up-to-date object
	**/
	public synchronized void lock_read(Client_itf c){
		if(lockState==State.WL){
			try{
				obj = writer.reduce_lock(this.id);
				this.readerList.add(this.writer);
				writer = null;
			}catch(RemoteException r){
				logger.log(WARNING,"Writer was lost");
			}finally{
		 		writer=null;
			}	
			this.readerList.add(c);		
			logger.log(INFO,"Client"+c.toString()+"was removed");
		
		}
		this.readerList.add(c);
		lockState=State.RL;
	}	
	/**Method lock_writer : similar to lock_write, invalidate both writer
 	* and readers.
	* @return obj : up-to-date object
	**/
	public synchronized void lock_write(Client_itf c){
		if(lockState==State.WL||this.readerList.size()!=0){	
			if(writer!=null){
				try{	
					obj = writer.invalidate_writer(this.id);
					writer = null;
				}catch(RemoteException r){
					logger.log(WARNING,"Writer was lost");
				}
			}
			for(Client_itf cli : readerList){
				try{
					cli.invalidate_reader(this.id);		
					this.readerList.remove(cli);
					logger.log(INGO,"Client"+cli.toString()+"retiré");
				}catch(RemoteException r){
					logger.log(WARNING,"Reader was lost");

				}
			}
		}
		try{
			this.readerList.remove(c);
		}catch(Exception e){
			logger.log(INFO,"List was empty. Whatever");	
		}	
		this.writer = c;
		this.lockState = State.WL;
		writer = c;
	}
}
