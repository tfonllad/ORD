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
        private Object lock; 
        private boolean busy;
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
                this.busy = false;
		this.lock = new Object();
		logger = Logger.getLogger("SharedObject");
		logger.setLevel(null);

	}

	// invoked by the user program on the client node
	public void lock_read() {
            boolean update = false;
            //lock.lock(); 
            synchronized(this.lock){
                while(busy){
                    try{
                        lock.wait();
                       }catch(InterruptedException e){}
                }
                synchronized(this) {
		        switch(this.lockState){
			        case RLC :
			        	this.lockState=State.RLT;
			        break;
			        case WLC:
				        this.lockState=State.RLT_WLC;
			        break;
			        default:
                                        update = true;
			        break;					
                        }
                }
             if(update){
                this.obj = client.lock_read(this.id);
        	this.lockState=State.RLT;
             }
           }
	}		

	// invoked by the user program on the client node
	public void lock_write() {
            boolean update = false;
            synchronized(lock){
		while(busy){
                    try{
                        lock.wait();
                        }catch(InterruptedException e){}
                }
                synchronized(this) {
                        switch(this.lockState){
                                case WLC:
		                logger.log(Level.INFO,this.lockState+" : local write");
                                this.lockState=State.WLT;
	    	                break;
	                        default: 
                                update = true;
		                break;
		        }
                }
                
       // lock.unlock();
                if(update){
                        this.obj = client.lock_write(this.id);
                        this.lockState=State.WLT;
                        logger.log(Level.INFO,"I can now write");
                }
            }
    }

	// invoked by the user program on the client node
	public synchronized void unlock() {
	//	this.lock.lock();
		switch(this.lockState){
			case RLT:
			lockState = State.RLC;
			break;
			case WLT:
			lockState = State.WLC;
			case RLT_WLC:
			lockState = State.WLC;	
		}
                notify();
		//this.available.signal();
		//logger.log(Level.INFO,"unlock : signal");	
	//	this.lock.unlock();
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
	//	this.lock.lock();
        synchronized(lock){
                busy = true;
        }
                State prev_state = this.lockState;
		while(this.lockState==State.WLT){
			try{
				//logger.log(Level.INFO,"await on client on :"+id+".");
				wait();
				//logger.log(Level.INFO,"client was reduced :"+id+".");
			}catch(InterruptedException i){}
		}
		switch(this.lockState){
			case WLT :
			this.lockState=State.RLC;
			break;
			case RLT_WLC:
			this.lockState=State.RLT;
			break;
			case WLC:
			this.lockState=State.RLC;
			break;
			default: 
			logger.log(Level.SEVERE,"Inconsistent Lock" );
                  logger.log(Level.SEVERE,"lock was "+prev_state+", lock i :"+this.lockState+".");
			break;
		}
        logger.log(Level.INFO,"I was <b>reduced</b> to "+this.lockState+".");
	//	this.available.signal();//réveil en chaîne des client-redacteur
	//	this.lock.unlock();
        synchronized(lock){
        busy = false;
        lock.notify();
        }
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
	//	this.lock.lock();
       synchronized(lock){
        busy = true;
       }
		while(this.lockState==State.RLT){
			try{	
				//logger.log(Level.INFO,"await on reader : "+id+".");
				wait();
				//logger.log(Level.INFO,"reader was released"+id+".");
			}catch(InterruptedException r){
				logger.log(Level.SEVERE,"Interrupted Exception");
			}
		}

		this.lockState=State.NL;
                        synchronized(lock){
                        busy = false;
                        lock.notify();
                }
	//	this.lock.unlock();
        
	}

	public synchronized Object invalidate_writer() {
		//this.lock.lock();
                synchronized(lock){
                busy = true;
                }
		while(this.lockState==State.WLT){
			try{
		//	logger.log(Level.INFO,"await on me");
			wait();
			}catch(InterruptedException t){}
		}

		this.lockState=State.NL;
	    logger.log(Level.INFO,"I was <b>invalidated</b> to :"+this.lockState+"." );
		//this.lock.unlock();	
                synchronized(lock){
                        busy = false;
                        lock.notify();
                }
		return obj;
                
	}

	public int getID(){
		return id;
	}
}
