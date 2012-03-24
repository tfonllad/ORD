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

    //Consistency
    private boolean reading;
    private boolean writing;
	private int waitingWriter;

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
		this.writing = false;
        this.waitingWriter = 0;
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
      	while(writing){
           	try{
               	wait();
            }catch(InterruptedException r){}
        }
		writing = true;
       	if(lockState==State.WL){
            try{
       	        obj = writer.reduce_lock(this.id); 
            }catch(RemoteException r){}
         } 
        this.writer = null;
        lockState = State.RL;
        this.readerList.add(c);
	    writing = false;
	    notify();
	}	

	/**Method lock_writer : similar to lock_write, invalidate both writer
 	* and readers.
	* @return obj : up-to-date object
	**/
	public synchronized void lock_write(Client_itf c){	
		Object o = obj;

        	while(writing){
            	try{
                	wait();
            	}catch(InterruptedException r){}
        	}
            //c is the only client here. There are no // lock_read
        	writing = true;
        	if(lockState==State.WL){
                try{
                    obj = writer.invalidate_writer(this.id);
                }catch(RemoteException r){}
            }
        	if(lockState==State.RL){
                this.readerList.remove(c);
             	for(Client_itf cli : readerList){
                    try{
               	        cli.invalidate_reader(this.id);
                    }catch(RemoteException r){}
                }
            }
         	writer = c;
            readerList.clear();
       	 	lockState = State.WL;
        	writing = false;
        	notify();	
	}
}
