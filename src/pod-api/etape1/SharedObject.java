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
	private int waitingWriter;
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
		this.waitingWriter = 0;
		this.lock = new ReentrantLock();
		this.available = this.lock.newCondition();
		logger = Logger.getLogger("SharedObject");
		logger.setLevel(Level.INFO);

	}

	// invoked by the user program on the client node
	public void lock_read() {
		switch(this.lockState){
			case RLC :
				this.lockState=State.RLT;
				logger.log(Level.INFO,"lock updated : RLT");
			break;
			case WLC:
				this.lockState=State.RLT_WLC;
				logger.log(Level.INFO,"lock updated : RLT_WLC");
			break;
			default:
				logger.log(Level.INFO,"lock_read request to client");
				this.obj = client.lock_read(this.id);
				this.lockState=State.RLT;
				logger.log(Level.INFO,"lock_read acquired");
				logger.log(Level.INFO,"lock_updated : RLT");
			break;					
		}
		
	}

	// invoked by the user program on the client node
	public void lock_write() {
		
		switch(this.lockState){
			case WLC:
				this.lockState=State.WLT;
				logger.log(Level.INFO,"lock updated : WLT");
			break;
			default: 
				if(this.waitingWriter==0){	
					logger.log(Level.INFO,"request lock_write");
					this.obj =  client.lock_write(this.id);
					this.lockState=State.WLT;
					logger.log(Level.INFO,"lock_write acquired");
				logger.log(Level.INFO,"lock_updated : RLT");
				}else{
					this.lockState=State.NL;
					logger.log(Level.INFO,"update lock NL");
	 				this.available.signal();
					logger.log(Level.INFO,"let writer go, request again");
					this.lock_write();
				}
			break;
		}
	}

	// invoked by the user program on the client node
	public void unlock() {
		this.lock.lock();
		switch(this.lockState){
			case RLT:
			lockState = State.RLC;
			logger.log(Level.INFO,"unlock : RLC");
			break;
			case WLT:
			lockState = State.WLC;
			logger.log(Level.INFO,"unlock : WLC");
			case RLT_WLC:
			lockState = State.WLC;
			logger.log(Level.INFO,"unlock : WLC");
		}
		this.available.signal();
		logger.log(Level.INFO,"unlock : signal");	
		this.lock.unlock();
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		this.lock.lock();
		if(this.lockState==State.WLT){
			while(this.lockState==State.WLT){
				try{
					logger.log(Level.INFO,"await on writer");
					this.available.await();
					logger.log(Level.INFO,"writer was released");
				}catch(InterruptedException i){}
			}
			this.lockState=State.RLC;
		}
		if(this.lockState==State.RLT_WLC){
			this.lockState=State.RLT;
		}
		if(this.lockState==State.WLC){
			this.lockState=State.RLC;
		}
		logger.log(Level.INFO,"lock reduced, lockstate = "+this.lockState.toString());
		this.lock.unlock();
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		this.lock.lock();
		while(this.lockState==State.RLT){
			try{
				this.waitingWriter+=1;
				logger.log(Level.INFO,"await on reader");
				this.available.await();
				logger.log(Level.INFO,"reader was released");
				this.waitingWriter-=1;
			}catch(InterruptedException r){}
		}
		this.lockState=State.NL;
		logger.log(Level.INFO,"reader invalidated, lockState NL="+this.lockState.toString() );
		this.lock.unlock();
	}

	public synchronized Object invalidate_writer() {
		this.lock.lock();
		while(this.lockState==State.WLT){
			try{
			logger.log(Level.INFO,"await on writer");
			this.available.await();
			logger.log(Level.INFO,"writer was released");
			}catch(InterruptedException t){}
		}
		this.lockState=State.NL;
		logger.log(Level.INFO,"writer invalidated, lockState NL="+this.lockState.toString() );
		this.lock.unlock();	
		return obj;
				
	}

	public int getID(){
		return id;
	}
}
