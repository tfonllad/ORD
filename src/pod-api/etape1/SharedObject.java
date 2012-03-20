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
	    logger.setLevel(Level.INFO);

	}

	// invoked by the user program on the client node
	public void lock_read() {
        boolean update = false;
   /*     synchronized(mutex){
            busy = false;
        }*/
        logger.log(Level.INFO,"entering lock_read");
        lock.lock();
		switch(this.lockState){
			case RLC :
				this.lockState=State.RLT;
                logger.log(Level.INFO,"reading in cache");
			break;
			case WLC:
				this.lockState=State.RLT_WLC;
				logger.log(Level.INFO,"reading in cache as previous writer");
			break;
			default:
                update = true;
			break;					
		}
        lock.unlock();
        logger.log(Level.FINE,"Release the mutex with :"+lockState+".");
        if(update){
        	logger.log(Level.INFO,"asking lock_read");
        	
        	//NL
        	this.lockState=State.RLT;
            this.obj = client.lock_read(this.id);
            
         }
	}

	// invoked by the user program on the client node
	public void lock_write() {
        boolean update = false;
        logger.log(Level.FINE,"asking lock_write before mutex");
        lock.lock();
        logger.log(Level.FINE,"asking lock_write after mutex");
        switch(this.lockState){
            case WLC:
                this.lockState=State.WLT;
                logger.log(Level.INFO,"writing with cache");
	        break;
		    default: 	
                update = true;
            break;
	    }
        lock.unlock();
        logger.log(Level.FINE,"Release the mutex with :"+lockState+".");
        if(update){
        	logger.log(Level.INFO,"asking lock_write");
        	this.lockState=State.WLT;
            this.obj = client.lock_write(this.id);
            
          /*  synchronized(mutex){
                if(busy){
                    logger.log(Level.WARNING,"re-ask for WLT");
                    this.lock_write();
                }else{
                    this.lockState=State.WLT;
                    logger.log(Level.INFO,"I can write with: "+lockState+".");
                }
            }*/
        }
    } 

	// invoked by the user program on the client node
	public void unlock(){
            logger.log(Level. INFO,"Unlock before mutex :"+lockState+".");
            this.lock.lock();
            logger.log(Level. INFO,"Unlock after mutex :"+lockState+".");
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
            logger.log(Level.WARNING,"Unlock with : "+lockState+".");
            break;
		}
                this.available.signal();	
                logger.log(Level.WARNING,"SIGNAL");
                this.lock.unlock();
	}

	// callback invoked remotely by the server
	public  Object reduce_lock() {
		this.lock.lock();
         switch(this.lockState){
            case WLT:
		        while(this.lockState==State.WLT){
			        try{		   
				        this.available.await();
		            }catch(InterruptedException i){}
		        }
                this.lockState = State.RLC;	
			break;
			case RLT_WLC:
			    this.lockState=State.RLT;
			break;
			case WLC:
			    this.lockState=State.RLC;
			break;
			default: 
                logger.log(Level.SEVERE,"reduce : Lock incoherent :"+lockState+".");
            break;
		}
        //busy = true; 
        logger.log(Level.INFO,"I was <b>reduced</b> to "+this.lockState+".");
		this.lock.unlock();
		return obj;
	}
    public Object invalidate_writer(){
        this.lock.lock();
        switch(this.lockState){
            case WLT:
                while(this.lockState==State.WLT){
			        try{		   
			            this.available.await();
		            }catch(InterruptedException i){}
		        }   
            break;
            case RLT_WLC:
                while(this.lockState==State.RLT_WLC){
 			        try{
                        this.available.await();
		            }catch(InterruptedException i){}
                }
                break;
                case WLC:
                        //do nothing
                break;
                default:
                    logger.log(Level.SEVERE,"inv_writer: Lock incoherent :"+lockState+".");
                break;
        }
        this.lockState = State.NL;
        //busy = true;
        logger.log(Level.INFO,"i was <b>invalidated</b> as a writer");
        this.lock.unlock();
        return obj;
    }

        public  void invalidate_reader(){
                this.lock.lock(); 
                switch(this.lockState){
                    case RLT:
                        while(this.lockState==State.RLT){
			        try{		   
			                this.available.await();
		                }catch(InterruptedException i){}
		        }
                    break;
                    case RLC:
                        //do nothing
                    break;
                    default:
                        logger.log(Level.SEVERE,"inv_reader: Lock incoherent :"+lockState+".");
                    break;
                }
                this.lockState = State.NL;

                logger.log(Level.INFO,"i was <b>invalidated</b> as a reader");
                this.lock.unlock();
        }
        
	public int getID(){
		return id;
	}
}
