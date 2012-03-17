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
		switch(this.lockState){
			case RLC :
				this.lockState=State.RLT;
			break;
			case WLC:
				this.lockState=State.RLT_WLC;
			break;
			default:

				this.obj = client.lock_read(this.id);
				this.lockState=State.RLT;
			break;					
		}
		
	}

	// invoked by the user program on the client node
	public void lock_write() {	
		switch(this.lockState){
			case WLC:
		        logger.log(Level.INFO,"my lock was :"+this.lockState+" :local write");
                 this.lockState=State.WLT;

	    	break;
			default: 
				this.obj =  client.lock_write(this.id);
				this.lockState=State.WLT;
                logger.log(Level.INFO,"I can now write");
    
			break;
            
		}
    }

	// invoked by the user program on the client node
	public void unlock() {
		this.lock.lock();
		switch(this.lockState){
			case RLT:
			lockState = State.RLC;
			break;
			case WLT:
			lockState = State.WLC;
			case RLT_WLC:
			lockState = State.WLC;	
		}
		this.available.signal();
		//logger.log(Level.INFO,"unlock : signal");	
		this.lock.unlock();
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		this.lock.lock();
		while(this.lockState==State.WLT){
			try{
				logger.log(Level.INFO,"await on client on :"+id+".");
				this.available.await();
				logger.log(Level.INFO,"client was reduced :"+id+".");
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
			logger.log(Level.SEVERE,"inconsistent lock");
			break;
		}
		this.available.signal();//réveil en chaîne des client-redacteur
		this.lock.unlock();
		return obj;
	}

	// callback invoked remotely by the server
	public void invalidate_reader() {
		this.lock.lock();
		while(this.lockState==State.RLT){
			try{	
				logger.log(Level.INFO,"await on reader : "+id+".");
				this.available.await();
				logger.log(Level.INFO,"reader was released"+id+".");
			}catch(InterruptedException r){
				logger.log(Level.SEVERE,"Interrupted Exception");
			}
		}

		this.lockState=State.NL;
		this.lock.unlock();
	}

	public Object invalidate_writer() {
		this.lock.lock();
		while(this.lockState==State.WLT){
			try{
			logger.log(Level.INFO,"await on me");
			this.available.await();
				}catch(InterruptedException t){}
		}

		this.lockState=State.NL;
	    logger.log(Level.INFO,"I can't write and my lock is :"+this.lockState+"." );
		this.lock.unlock();	
		return obj;
				
	}

	public int getID(){
		return id;
	}
}
