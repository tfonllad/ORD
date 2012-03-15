import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.HashMap;
import java.lang.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	// Vérifier qu'on a le droit de rajouter un attribut. Je suis pas
	// certain des consignes -> OK tant qu'ils sont en privé et qu'on change pas l'interface
	
	private static HashMap<Integer,SharedObject> hmID;
	private static Server_itf server;
	private static Client_itf client;	
	private static Logger logger;	

	public Client() throws RemoteException {
		super();
	}

	public static Object getObject(int id){
		return hmID.get(id).obj;
	}
	
///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer -> OK
	public static void init() {
		logger = Logger.getLogger("Client");
		logger.setLevel(Level.INFO);
		hmID = new HashMap<Integer,SharedObject>();
		try{  	
			client = new Client();	
			int port = 1099; 
			server = (Server_itf)Naming.lookup("//"+"localhost"+":"+String.valueOf(port)+"/Server");
			logger.log(Level.FINE,"Server found");
		}catch(Exception e){
			logger.log(Level.SEVERE,"Server not found");
			System.exit(0);
		}	
	}

	// lookup in the name server
	/**Method lookup : get the Shared object from the server. If the object
 	* is not found , return a SharedObject with obj==null
	*@param name : name of registred object
	*@return so : the local SharedObject
	**/
	public static SharedObject lookup(String name){
		int id;
		SharedObject so=null;
		
		try{
			id = server.lookup(name);
			if(hmID.containsKey(id)){
			 	so = hmID.get(id);
			}else{
				logger.log(Level.FINE,"lookup");
				id = server.lookup(name);
				if(id==0){
					so = null;
				}else{
					so = new SharedObject(id,null,(Client)client);
					hmID.put(id,so);
				}
			}
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(0);
		}	
		return so;
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		//Enregistrement local	}
		int id =((SharedObject) so).getID();
		System.out.println(id);
		try{
			server.register(name,id);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(0);
		}
	}

	// creation of a shared object
	/**Method create : give o to the server wich will deliver id and cache
 	* it. 
	* @param o : object to share.
	* @return so : local representation of the object.
	*/
	public static SharedObject create(Object o) {
		int id = -1;
		SharedObject so = null;

		try{		
			id = server.create(o);		
			so = new SharedObject(id,o,(Client)client);
			hmID.put(id,so);
				
	 	}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(0);
		}
		return so;		
		
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	/** Method lock_read request a read lock from the server
	/*@param id : the id of the object to lock
	/*@return o : up-to-date object retrived from the serer
	**/
	public static Object lock_read(int id) {
		Object o = null;
		logger.log(Level.INFO,"request lock_read");
		try{
			o = server.lock_read(id,client);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(0);
		}
		logger.log(Level.INFO,"obtained lock_read");
		return o;	
	}

	/** Method lock_write request a write lock from the server
	*@param id : the id of the object to lock
	*@return o : up-to-date object retrived from the serer
	**/
	public static Object lock_write(int id) {
		Object o = null;
		logger.log(Level.INFO,"request lock_write");
		try{
			o = server.lock_write(id,client);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(0);
		}
		logger.log(Level.INFO,"obtained lock_write");
		return o;
	}

	// receive a lock reduction request from the server
	/**Method reduce_lock : called on the client when he has WLC/WLT he will
	 * keep the right to read but loose WLC/WLT.
	 *@param id : the id of the targeted object.
	 *@return o : the up-to-date object given back to the server
	**/
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		Object o;
		logger.log(Level.INFO,"recieved reduce_lock");
		o = so.reduce_lock();
		logger.log(Level.INFO,"lock was reduced");
		return o;
	}

	// receive a reader invalidation request from the server
	/**Method invalidate_reader : release RLT/RLC
	 *@param id : the id of the targeted object.
	**/
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		logger.log(Level.INFO,"recieved invalidate_reader");
		so.invalidate_reader();
		logger.log(Level.INFO,"reader was invalidated");
	}

	// receive a writer invalidation request from the server
	/**Method invalidate_writer : release WLC/WLT/WLC_RLT
	 *@param id : the id of the targeted object.
	 *@return o : the up-to-date object given back to the server
	**/
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		Object o;
		logger.log(Level.INFO,"recieved invalidate_writer");
		o = so.invalidate_writer();
		logger.log(Level.INFO,"writer was invalidated");
		return o;
	}	
}

