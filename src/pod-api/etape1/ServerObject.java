/**@version etape1
**/
import java.util.ArrayList;
import java.rmi.*;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Collections;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ServerObject{

	//identification	
	private int id;
	private List<Client_itf> readerList; 
	private Client_itf writer;	
	public Object obj;		
	private static Logger logger;	
	private State lockState;
	private Condition read;
	private Condition write;
	private int waitingWriter;
	private ReentrantLock lock;
	private int waitingReader;

	/**Constructor ServerObject
	*@param id : the unique id.
	*@param o : cached object. Used for lookup and reader without writer
	*invalidation
	**/
	public ServerObject(int id,Object o){
		this.id = id;	
		this.obj = o;
		this.readerList = Collections.synchronizedList(new ArrayList<Client_itf>());
		logger = Logger.getLogger("ServerObject");
		logger.setLevel(Level.INFO);
		this.lock = new ReentrantLock();
                List list = Collections.synchronizedList(new ArrayList<Client_itf>());
                this.readerList = list;
		this.read = lock.newCondition();
		this.write = lock.newCondition();
		this.waitingWriter = 0;
		this.waitingReader =0;
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
	public void lock_read(Client_itf c){
		this.lock.lock();
		while(!((writer==null)&&(waitingWriter==0))){
			try{
				this.waitingReader=+1;
                                logger.log(Level.WARNING,"read.await");
				this.read.await();
                                 logger.log(Level.WARNING,"débloquage await");
			}catch(InterruptedException e){
				this.waitingReader-=1;
				}
		}
		
		if(lockState==State.WL){
			try{
				obj = writer.reduce_lock(this.id);
				this.readerList.add(this.writer);
				synchronized(this.readerList){
					this.readerList.add(this.writer);
				}
				writer = null;
				logger.log(Level.INFO,"writer was removed");
			}catch(RemoteException r){
				logger.log(Level.WARNING,"Writer was lost");
			}finally{
		 		writer=null;
			}
				synchronized(this.readerList){
				this.readerList.add(c);	
			}					
			logger.log(Level.INFO,"Reader was added");
		
		}
		this.readerList.add(c);
		lockState=State.RL;

		if(waitingWriter==0){
			read.signal();
		}else{
			write.signal();
                        logger.log(Level.WARNING,"write.signal()");
		}
		this.lock.unlock();
	}	
	/**Method lock_writer : similar to lock_write, invalidate both writer
 	* and readers.
	* @return obj : up-to-date object
	**/
	public void lock_write(Client_itf c){	
		this.lock.lock(); // il faut tester le lock du writer
                                  // si le WL passe en WLC, il ne fera plus
                                  // jamais de unlock() mais on aura 
                                  // writer != null toujours
		while(!((this.readerList.size()==0)&&(writer==null))){
			try{	
                                this.waitingWriter+=1;
                                logger.log(Level.WARNING,"write.await");
				write.await();
                                logger.log(Level.WARNING,"débloquage write");
        			this.waitingWriter-=1;
                        }catch(InterruptedException r){}
		}
		if(writer!=null){
				try{	
					obj = writer.invalidate_writer(this.id);
					writer = null;
				}catch(RemoteException r){
					logger.log(Level.WARNING,"Writer was lost");
				}
			}
		synchronized(readerList){
		if(this.readerList.size()!=0){
		
				for(Client_itf cli : readerList){
					try{
						cli.invalidate_reader(this.id);		
						this.readerList.remove(cli);
						logger.log(Level.INFO,"Readers removed (lock_write)");
					}catch(RemoteException r){
						logger.log(Level.WARNING,"Reader was lost");

					}
				}
			}
		}
		
		try{
			this.readerList.remove(c);
			logger.log(Level.INFO,"new writer is not in reader List");
		}catch(Exception e){
			logger.log(Level.INFO,"List was empty. Whatever");	
		}	
		this.writer = c;
		this.lockState = State.WL;
		writer = c;

		if(waitingWriter==0){
			read.signal();
		}else{
			write.signal();
                        logger.log(Level.WARNING,"write.signal()");
		}
		this.lock.unlock();
	}
}
