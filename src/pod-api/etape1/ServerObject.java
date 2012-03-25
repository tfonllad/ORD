/**@version etape1
**/
import java.util.ArrayList;
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
	private State lockState;

	/**Constructor ServerObject
	*@param id : the unique id.
	*@param o : cached object. Used for lookup and reader without writer
	*invalidation
	**/
	public ServerObject(int id,Object o){
		this.id = id;	
		this.obj = o;
        this.writer = null;
		this.readerList = new ArrayList<Client_itf>();
		logger = Logger.getLogger("ServerObject");
		logger.setLevel(Level.SEVERE);
        //Consistency
 
        this.lockState = State.NL;
	}

	public int getID(){
		return this.id;
	}
	public enum State{
		NL,
		WL,
		RL;
	}

	/**Method lock_read : called by client to get lock on the object.The
 	* method call reduce_lock on the writer if not null
	* @return o : up-to-date object
	**/
	public synchronized void lock_read(Client_itf c){	
      	Object o = obj;
        switch(this.lockState){
            case WL :
                try{
       	            obj = writer.reduce_lock(this.id); 
                }catch(RemoteException r){}
                lockState = State.RL;
                writer = null;
            break;

            case NL :
                lockState = State.RL;
            break;

            case RL:
                ////
            break;
        }
     this.readerList.add(c);
	}	

	/**Method lock_writer : similar to lock_write, invalidate both writer
 	* and readers.
	* @return obj : up-to-date object
	**/
	public synchronized void lock_write(Client_itf c){	
		Object o = obj;
        switch(lockState){
            case RL :
                this.readerList.remove(c);
           	    for(Client_itf cli : readerList){
                    try{
                        cli.invalidate_reader(this.id);
                    }catch(RemoteException r){}
                }
                this.lockState = State.WL;
            break;
            case WL :
                try{
                    obj = writer.invalidate_writer(this.id);
                }catch(RemoteException r){}
            break;
            case NL:
                this.lockState = State.WL;
             break;
            default : break;
        }
      	writer = c;
        readerList.clear();
	}
}
