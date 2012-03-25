/**@version etape1
* 
**/
import java.util.HashMap;
import java.rmi.registry.*;
import java.rmi.*;
import java.net.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Server extends UnicastRemoteObject implements Server_itf{

	private HashMap<String,Integer> hmName; 
	private HashMap<Integer,ServerObject> hmID;
	private static int cpt; 
	private ReentrantLock mutex;
	private static Logger logger;

	public Server() throws RemoteException{
		super();
		this.hmName = new HashMap<String,Integer>();
		this.hmID = new HashMap<Integer,ServerObject>();
		this.mutex = new ReentrantLock();
        this.cpt = 0;

		logger = Logger.getLogger("Server");
		logger.setLevel(Level.SEVERE);
	}
    
    /** Propagate the lock_read to the Server Object
     * @param id : identification of the Object
     * @param client : client who request lock_read
     * @return obj : the up-to-date object
     * @throws java.rmi.RemoteException
     */
	public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException{	
		logger.log(Level.FINE,"propagation lock_read");
        //recover the ServerObject with thi id
		ServerObject so = this.hmID.get(id);
        //and ask lock_read on it
		so.lock_read(client);
		logger.log(Level.FINE,"fin propagation lock_read");
		return so.obj;
    }

     /** Propagate the lock_write to the Server Object
     * @param id : identification of the Object
     * @param client : client who request lock_write
     * @return obj : the up-to-date object
     */  
    public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException{
		logger.log(Level.FINE,"propagation lock_write");
		ServerObject so = this.hmID.get(id);
        ServerObject so_prev = so;
		so.lock_write(client); 
		logger.log(Level.FINE,"fin propagation lock_write");
		return so.obj;
	}
	
	/**Method lookup : return the ID of object "name" if it was registered,
 	* otherwise return id = 0
	* @param name under which the Object was registred
	* @return id
	* @throws RemoteException
	**/
	public int lookup(String name) throws java.rmi.RemoteException{
		int id;
        logger.log(Level.INFO,"lookup");
		if(!this.hmName.containsKey(name)){
            //the object is not on the server
			logger.log(Level.WARNING,"Name not found");
			id = 0;
		}else{
			logger.log(Level.INFO,"Name found");	
			id = this.hmName.get(name);
		}
		return id;		
	} 
	
	/** Method Register : register the name and ID of a ServerObject
	* @param name : the name of the object
	* @param ID : the ID
	* @throws RemoteException
	**/
	public void register(String name,int id) throws java.rmi.RemoteException{
		ServerObject so = this.hmID.get(id);
		if(!hmName.containsKey(name)){
			this.hmName.put(name,id);
		}else{ 	
			logger.log(Level.WARNING,"Name already registred");
		}
        //release the mutex
		this.mutex.unlock();
	}

	/** Method create : create Server Object, add it to hmID and return ID
	* @param o : the object to create 
	* @return cpt : the ID
	* @throws RemoteException
	**/
	public int create(Object o) throws java.rmi.RemoteException{
        //take mutex to ensure that the create will be followed by register
		this.mutex.lock();
		cpt = cpt+1;
		ServerObject so = new ServerObject(cpt,o);
		this.hmID.put(cpt,so);
		logger.log(Level.FINE,"Done creating objec. ID ="+cpt+".");
		return cpt;
	}

	/** Main : create server and server name, wait for connexion
	**/
	public static void main(String args[]){
		int port;
		String url;
		Registry registry;	
		Server server;		
		try{
			server = new Server();
			port = 1099;
			registry = LocateRegistry.createRegistry(port);
			url ="//"+"localhost"+":"+String.valueOf(port)+"/Server";
			System.out.println("URL du serveur : "+url);
			Naming.bind(url,server);
			System.out.println("Server is now running ...");

		}catch(Exception e){
			logger.log(Level.SEVERE,"Failed to initialize Server");
			System.exit(0);
		}				
	}
}
