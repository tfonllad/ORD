import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id;
	public Object obj;		
	private State lockState;
	private ReentrantLock lock;
	private Condition available;
	private Client client;
	private static Logger logger;
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
		this.available = this.lock.newCondition();
		logger = Logger.getLogger("SharedObject");
		logger.setLevel(null);

	}

	// invoked by the user program on the client node
	public void lock_read() {
            boolean update = false;
            //lock.lock();
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

				this.lockState=State.RLT;
			break;					
        }
                 }//lock.unlock();
            if(update){
                this.obj = client.lock_read(this.id);
            }
	}		

	// invoked by the user program on the client node
	public void lock_write() {
                boolean update = false;
		//lock.lock();
                synchronized(this) {
        switch(this.lockState){
                case WLC:
		logger.log(Level.INFO,this.lockState+" : local write");
                this.lockState=State.WLT;
	    	break;
	        default: 
                update = true;
		this.lockState=State.WLT;
   		break;
            
		}
    }
       // lock.unlock();
        if(update){
            this.obj = client.lock_write(this.id);
            logger.log(Level.INFO,"I can now write");
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
			logger.log(Level.SEVERE,"inconsistent lock reduced_lock on "+prevState+"." );
			break;
		}
        logger.log(Level.INFO,"I was <b>reduced</b> to "+this.lockState+".");
	//	this.available.signal();//réveil en chaîne des client-redacteur
	//	this.lock.unlock();
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
	//	this.lock.lock();
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
	//	this.lock.unlock();
	}

	public synchronized Object invalidate_writer() {
		//this.lock.lock();
		while(this.lockState==State.WLT){
			try{
		//	logger.log(Level.INFO,"await on me");
			wait();
			}catch(InterruptedException t){}
		}

		this.lockState=State.NL;
	    logger.log(Level.INFO,"I was <b>invalidated</b> to :"+this.lockState+"." );
		//this.lock.unlock();	
		return obj;
				
	}

	public int getID(){
		return id;
	}
}
