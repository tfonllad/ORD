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
	    this.lockState = State.NL;
	    	
        logger = Logger.getLogger("ServerObject");
		logger.setLevel(Level.WARNING);
    }

	public int getID(){
		return this.id;
	}

	public enum State{
		NL,
		WL,
		RL;
	}

	/**Method lock_read : reduce_lock on writer, switch state to RL.
	**/
	public synchronized void lock_read(Client_itf c){	
      	Object o = obj;
        try{
            switch(this.lockState){
                case WL :
                    obj = writer.reduce_lock(this.id); //WLT/WLC -> RLC, RLT_WLC -> RLT  
                    this.readerList.add(writer);
                break;

                case NL :
                    ////
                break;

                case RL:
                    ////
                break;
            }
        }catch(RemoteException r){
            logger.log(Level.WARNING,"Could not retrieve object. Reverting state to last update in cache");
        }finally{
            lockState = State.RL;
            this.readerList.add(c);
            writer = null;
        }
    }	

	/**Method lock_writer : invalidate readers and writers and switch state to WL
	**/
	public synchronized void lock_write(Client_itf c){	
		Object o = obj;
        
        switch(lockState){
            case RL :
                this.readerList.remove(c);
           	    for(Client_itf cli : readerList){
                    try{
                        cli.invalidate_reader(this.id);
                    }catch(RemoteException e){
                        logger.log(Level.WARNING,"A reader has been lost");
                    }
                }
            break;
            case WL :
                try{
                    obj = writer.invalidate_writer(this.id);     
                }catch(RemoteException e){
                    logger.log(Level.WARNING,"A writer was lost");
                }
            break;
            case NL:
                    ////
            break;
            default : break;
        }
        this.lockState = State.WL;
      	writer = c;
        readerList.clear();
     }
}
