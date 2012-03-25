import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id;
	public Object obj;		
	private State lockState;
	private Client client;
	private static Logger logger;
    private ReentrantLock lock;
    private Condition available;
    
    public enum State{	
		NL,
		RLT,
		WLT,
		RLC,
		WLC,
		RLT_WLC;
	}

	public SharedObject(int id,Object object,Client c){
		this.id = id;
		this.obj = object;
		this.lockState = State.NL;
		this.client = c;
        this.lock = new ReentrantLock();
        this.available = lock.newCondition();
        
		logger = Logger.getLogger("SharedObject");
	    logger.setLevel(Level.SEVERE);
	}

	// invoked by the user program on the client node
	public void lock_read() {
        boolean update = false;

        //////////
        lock.lock();
	        logger.log(Level.INFO,"lock_read() :"+this.lockState);
            switch(this.lockState){
			case RLC :
				this.lockState=State.RLT;
                logger.log(Level.INFO,"reading in cache");
			break;
			case WLC:
				this.lockState=State.RLT_WLC;
				logger.log(Level.INFO,"reading in cache as previous writer");
			break;
			case NL:
                this.lockState = State.RLT;
                update = true;
			break;
            default:
                logger.log(Level.SEVERE,"lock_reade : Lock incoherent :"+lockState+".");
            break;
		}
        lock.unlock();
        /////////
        logger.log(Level.FINE,"lock_read : release the lock with :"+lockState+".");
        if(update){
            logger.log(Level.FINE,"lock_read : before propagation :"+lockState+".");
            this.obj = client.lock_read(this.id);
            logger.log(Level.INFO,"lock_read : after lock_read :"+lockState+".");
        }
	}

	// invoked by the user program on the client node
	public void lock_write() {
        boolean update = false;
        ///////////
        lock.lock();
        logger.log(Level.INFO,"lock_write() "+this.lockState+".");
        switch(this.lockState){
            case WLC:
                this.lockState=State.WLT;
                logger.log(Level.INFO,"writing with cache");
	        break;
            case RLC:
                this.lockState = State.WLT;
                update = true;
		    case NL: 	
                this.lockState = State.WLT;
                update = true;
            break;
            default:
                 logger.log(Level.SEVERE,"lock_write : Lock incoherent :"+lockState+".");
            break; 

	    }
        logger.log(Level.INFO,"lock_write : release the lock with :"+lockState+".");
        lock.unlock();
        ////////
             
        if(update){
            logger.log(Level.FINE,"lock_write : before propagation :"+lockState+".");
            this.obj = client.lock_write(this.id);
            logger.log(Level.INFO,"lock_write : after propagation :"+lockState+".");
        }

    } 

	// invoked by the user program on the client node
	public void unlock(){
        logger.log(Level. FINE,"unlock() "+lockState+".");
        this.lock.lock();
        logger.log(Level.FINE,"unlock taking  mutex :"+lockState+".");
        switch(this.lockState){
	        case RLT:
			    lockState = State.RLC;
			break;
			case WLT:
			    lockState = State.WLC;
			case RLT_WLC:
			    lockState = State.WLC;	
            break;
            default:
                logger.log(Level.INFO,"Incoherent unlock with : "+lockState+".");
            break;
		}
       this.available.signal();	
       logger.log(Level.INFO,"SIGNAL : "+ this.lockState);
       this.lock.unlock();
	}

	// callback invoked remotely by the server
	public Object reduce_lock() {
        this.lock.lock();

         switch(this.lockState){
            case WLT:
		        while(this.lockState==State.WLT||this.lockState==State.RLT_WLC){
			        try{		   
				        this.available.await();
                        //WLT->WLC. lock_write -> WLT 
                        //          lock_read  -> RLT_WLC
		            }catch(InterruptedException i){
                        i.printStackTrace();  
                    }
		        }
                if(this.lockState != State.WLC){
                    logger.log(Level.SEVERE,"Lock is : "+this.lockState+" instead of WLC");
                }
                this.lockState = State.RLC;	
			break;
			case WLC:
			    this.lockState=State.RLC;
			break;
            case RLT_WLC:
			    this.lockState=State.RLT;
			break;	
         	default: 
                logger.log(Level.SEVERE,"reduce : Lock incoherent :"+lockState+".");
            break;

		}
        logger.log(Level.INFO,"I was <b>reduced</b> to "+this.lockState+".");

		this.lock.unlock();
		return obj;
	}

    public Object invalidate_writer(){
        this.lock.lock();
        switch(this.lockState){
           case WLT:
                while(this.lockState==State.WLT||this.lockState==State.RLT_WLC){
			        try{		   
			            this.available.await();
                        //WLT->WLC : lock_write -> WLT
                        //           lock_read -> RLT_WLC
		            }catch(InterruptedException i){
                        i.printStackTrace();
                    }
                }
                if(this.lockState!=State.WLC){
                    logger.log(Level.SEVERE,"Lock is : "+this.lockState+" instead of WLC");
                }
                this.lockState = State.NL;
           break;

           case RLT_WLC:
                while(this.lockState==State.RLT_WLC||this.lockState==State.WLT){
 			        try{
                        this.available.await();
                        //RLT_WLC->WLC : lock_read -> RLT_WLC
                        //             : lock_write -> WLT
		            }catch(InterruptedException i){ 
                        i.printStackTrace();
                    }       
                }
                if(this.lockState != State.WLC){
                    logger.log(Level.SEVERE,"Lock is : "+this.lockState+" instead of WLC");
                }
                this.lockState = State.NL;
           break;
           case WLC:
                this.lockState = State.NL;
           break;
          
           default:
                    logger.log(Level.SEVERE,"inv_writer: Lock incoherent :"+lockState+".");
           break;
        }
        logger.log(Level.INFO,"i was <b>invalidated</b> as a writer : "+this.lockState+".");
        this.lock.unlock();
        return obj;
    }

    public void invalidate_reader(){
        this.lock.lock(); 
        logger.log(Level.INFO,"Invalidate reader of lock : "+this.lockState+".");
         switch(this.lockState){
             case RLT:
                 while(this.lockState==State.RLT){
			        try{		   
			            this.available.await();
                        //RLT -> RLC  lock_read -> RLT
                        //            lock_write -> WLT but propagation to the server
		            }catch(InterruptedException i){
                        i.printStackTrace();
                    }
		         }
                 if(this.lockState!=State.RLC){
                     if(this.lockState != State.WLT){
                            logger.log(Level.SEVERE,"Lock is "+this.lockState+" instead of WLT.");
                     }
                 }else{
                    this.lockState = State.NL;
                 }
              break;
              case RLC:
                this.lockState = State.NL;
              break;
              case WLT ://still a reader : happens when reader is set to WLT, and invalidated before the lock_write is propagated
                        //to the server. He will invalidate any other writer/reader when he tooks the lock.
                        // do nothing  
                        logger.log(Level.WARNING,"inv_reader OK if WLC : " +this.lockState+".");
              break;
              default:
                logger.log(Level.SEVERE,"inv_reader: Lock incoherent :"+lockState+".");
              break;
        }    
        logger.log(Level.INFO,"i was <b>invalidated</b> as a reader : "+this.lockState+".");      
        this.lock.unlock();
    }
        
	public int getID(){
		return id;
	}
}
