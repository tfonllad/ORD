<<<<<<< HEAD
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.HashMap;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	// Vérifier qu'on a le droit de rajouter un attribut. Je suis pas
	// certain des consignes
	
	private static HashMap<Integer,SharedObject> localHMID;
	private static Server_itf server;
	private static Client_itf client;	
	
	public static Object getObject(int id){
		return localHMID.get(id).obj;
	}
	public static SharedObject getSharedObject(int id){
		return localHMID.get(id);
	}

	public Client() throws RemoteException {
		super();
		localHMID = new HashMap<Integer,SharedObject>();
	}
	

///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
		localHMID = new HashMap<Integer,SharedObject>();
		Client client;	
		int port;
		String host;
		Registry registry;	
		
		//Connexion
		try{  	
			client = new Client();	
			port = 1099; 
			registry = LocateRegistry.getRegistry(host,port);
			server = (Server_itf) Naming.lookup("//"+host+":"+port+"/Server");
		}catch(Exception e){
			System.out.println("Faild to connect to the Server");
			e.printStackTrace();
		}
	}
	
	// lookup in the name server

	public static SharedObject lookup(String name){
		int id;
		SharedObject so;
		
		try{
			id = server.lookup(name);
			so = new SharedObject(id,server.getSharedObject(id));

		}catch(RemoteException r){
		}finally{
			so = null;
		}			
		return so;
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		//Enregistrement local		
		int objID =((SharedObject) so).getID();
		try{
			server.register(name,objID);
		}catch(RemoteException r){
			r.printStackTrace();
		}
	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		//communication avec le server, renvoit un id idObj
		int id;
		SharedObject so;

		try{			
			id = server.create(o);		
			server.initialize(id,client);
			so = new SharedObject(id,o);
			localHMID.put(id,so);
	
	 	}catch(RemoteException r){
			r.printStackTrace();
		}finally{
			so=null;
	
		return so;		
		}
		
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
		so = hmID.get(id);
		Object o;
		
		o = server.lock_read(id,client);	
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
		
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		SharedObject so = localHMID.get(id);
		Object o;

		o = so.reduce_lock();
		
		//TODO mettre à jour l'object dans SO

		return o;
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
		SharedObject so = localHMID.get(id);
		
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		SharedObject so = localHMID.get(id);
		Object o;

		so = so.invalidate_writer();
		//TODO mettre a jour obj dans le SO
		return o;
	}	
}

=======
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
	
	
	private static HashMap<Integer,SharedObject> hmID;
	private static Server_itf server;
	private static Client_itf client;	
	private static Logger logger = Logger.getLogger(Client.class.getName());	

	public Client() throws RemoteException {
		super();
	}

///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer -> OK
	public static void init() {
		logger.setLevel(Level.SEVERE);
        hmID = new HashMap<Integer,SharedObject>();
		try{  	
			client = new Client();	
			int port = 1099; 
			server = (Server_itf)Naming.lookup("//"+"localhost"+":"+String.valueOf(port)+"/Server");
			logger.log(Level.FINE,"Server found");
		}catch(Exception e){
			logger.log(Level.SEVERE,"Server not found");
			System.exit(-1);
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
		SharedObject so = null;	
		try{
			id = server.lookup(name);
			//if the object was already there
			if(hmID.containsKey(id)){
                //we return it
			 	so = hmID.get(id);
			}else{
				if(id==0){
                    //if the object doesn't exist on the server
					so = null;
                    //we return null
				}else{
                    //we create a local copy
					so = new SharedObject(id,null,(Client)client);
                    //the actual Object will be recovered after a lock request
					hmID.put(id,so);
				}
			}
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}	
		return so;
	}		
	
	// binding in the name server
    /** Register a SharedObject under a specific name
     * @param name : the name of the SharedObject
     * @param so : the ServerObject
     */
	public static void register(String name, SharedObject_itf so) {
		int id =((SharedObject) so).getID();
		try{
			server.register(name,id);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
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
            //get an id from the server
			id = server.create(o);
            //create a local representation
			so = new SharedObject(id,o,(Client)client);
			hmID.put(id,so);
				
	 	}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
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
		try{
			o = server.lock_read(id,client);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}
        if(o==null){
            logger.log(Level.SEVERE,"NULL object recovered on LR");
        }
		return o;	
	}

	/** Method lock_write request a write lock from the server
	*@param id : the id of the object to lock
	*@return o : up-to-date object retrived from the serer
	**/
	public static Object lock_write(int id) {
		Object o = null;
		try{
			o = server.lock_write(id,client);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}
        if(o==null){
            logger.log(Level.SEVERE,"NULL object recovered on LW");
        }
		return o;
	}

	// receive a lock reduction request from the server
	
    /**Method reduce_lock : called on the client when he has WLC/WLT he will
	 * keep the right to read but loose WLC/WLT.
	 *@param id : the id of the targeted object.
	 *@return the up-to-date object given back to the server
	**/

	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		return hmID.get(id).reduce_lock();
	}


	// receive a reader invalidation request from the server
	
    /**Method invalidate_reader : release RLT/RLC
	 *@param id : the id of the targeted object.
	**/

	public void invalidate_reader(int id) throws java.rmi.RemoteException {
        hmID.get(id).invalidate_reader();
	}

	// receive a writer invalidation request from the server
    
	/**Method invalidate_writer : release WLC/WLT/WLC_RLT
	 *@param id : the id of the targeted object.
	 *@return o : the up-to-date object given back to the server
	**/
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {        
		return hmID.get(id).invalidate_writer();
	}	
}

>>>>>>> upstream/master
