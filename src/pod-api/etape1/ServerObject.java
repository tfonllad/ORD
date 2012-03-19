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

    //Consistency
    private int nbReader;
	private ReentrantLock lock;
	private Condition read;
	private Condition write;
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
		this.readerList = Collections.synchronizedList(new ArrayList<Client_itf>());
		logger = Logger.getLogger("ServerObject");
		logger.setLevel(Level.INFO);
        	//Consistency
		this.lock = new ReentrantLock();
		this.read = lock.newCondition();
		this.write = lock.newCondition();;
		this.writing = false;
        	this.waitingWriter = 0;
        	this.nbReader = 0;
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
		this.lock.lock();	

        	Object o = obj;
        	while(writing){
            	try{
                	read.await();
            	}catch(InterruptedException r){}
        	}
		writing = true;
        	if(lockState==State.WL){
            		try{//only on reader should enter here
                		obj = writer.reduce_lock(this.id); 
            		}catch(RemoteException r){}
        	}
        	lockState = State.RL;
        	writer=null;
        	logger.log(Level.SEVERE,"ajout list");
        	readerList.add(c); 
		writing = false;
        	if(waitingWriter==0){
            		read.signal();
        	}else{
            		write.signal();
        	}
		this.lock.unlock();
	}	

	/**Method lock_writer : similar to lock_write, invalidate both writer
 	* and readers.
	* @return obj : up-to-date object
	**/
	public synchronized void lock_write(Client_itf c){	

		this.lock.lock();
		Object o = obj;

        	while(writing){
            	waitingWriter+=1;
            	try{
                	write.await();
            	}catch(InterruptedException r){}
            		waitingWriter-=1;
        	}
        	writing = true;
        	if(lockState==State.WL){
            		try{
                		obj = writer.invalidate_writer(this.id);
                	}catch(RemoteException ni){}
        	}	
        	writer = c;
        	if(lockState==State.RL){
            		logger.log(Level.SEVERE,"début liste invalidation");
             		for(Client_itf cli : readerList){
                        	 try{
                	    	cli.invalidate_reader(this.id);
              		  	}catch(RemoteException r){}
            		}
            		logger.log(Level.SEVERE,"fin liste invalidation");
        	} 
        	logger.log(Level.SEVERE,"clear list");
        	readerList.clear();
       	 	lockState = State.WL;
        	writing = false;
        	if(waitingWriter==0){
            		read.signal();
       	 	}else{
            		write.signal();
        	}
	    this.lock.unlock();
	}
}
