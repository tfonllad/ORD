import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.HashMap;
//import org.apache.log4j.Logger;
//import org.apache.log4j.FileAppender;
//import org.apache.log4j.XMLLayout;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	// Vérifier qu'on a le droit de rajouter un attribut. Je suis pas
	// certain des consignes -> OK tant qu'ils sont en privé et qu'on change pas l'interface
	
	private static HashMap<Integer,SharedObject> hmID;
	private static Server_itf server;
	private static Client_itf client;	
//	private static final Logger logger = Logger.getLogger(Client.class);	
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
		hmID = new HashMap<Integer,SharedObject>();
		try{  	
			client = new Client();	
			int port = 1099; 
			server = (Server_itf) Naming.lookup("//"+InetAddress.getLocalHost().getHostName()+":"+port+"/Server");
		//	logger.log(Level.INFO,"Connecting to the Server");
		}catch(Exception e){
		//	logger.fatal("server not found, exception",e);
			e.printStackTrace();
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
		SharedObject so;
		
		try{
		//	logger.log(Level.INFO,"Looking-up to the server");
			id = server.lookup(name);
			if(hmID.containsKey(id)){
			 	so = hmID.get(id);
			}else{
				so = new SharedObject(id,null,(Client)client);
				hmID.put(id,so);
			}
		}catch(RemoteException r){
		//	logger.fatal("Connexion lost, exception",r);
		}finally{
			so = null;
		}			
		return so;
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		//Enregistrement local		
		int id =((SharedObject) so).getID();
		try{
		//	logger.log(Level.INFO,"registering name");
			server.register(name,id);
		}catch(RemoteException r){
		//	logger.fatal("Connexion lost, exception",r);
			r.printStackTrace();
		}
	}

	// creation of a shared object
	/**Method create : give o to the server wich will deliver id and cache
 	* it. 
	* @param o : object to share.
	* @return so : local representation of the object.
	*/
	public static SharedObject create(Object o) {
		int id;
		SharedObject so;

		try{	
		//	logger.log(Level.INFO,"creating object");		
			id = server.create(o);		
			so = new SharedObject(id,o,(Client)client);
			hmID.put(id,so);
				
	 	}catch(RemoteException r){
		//	logger.fatal("Connexion lost, exception",r);
			r.printStackTrace();
		}finally{
			so=null;
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
		Object o;
		try{
		//	logger.log(Level.INFO,"lock_read to the server");
			o = server.lock_read(id,client);
		}catch(RemoteException r){
		//	logger.fatal("Connexion lost, exception",r);
			r.printStackTrace();
		}finally{
			o = null;
		}
		return o;	
	}

	/** Method lock_write request a write lock from the server
	*@param id : the id of the object to lock
	*@return o : up-to-date object retrived from the serer
	**/
	public static Object lock_write(int id) {
		Object o;
		try{
		//	logger.log(Level.INFO,"lock_write to the server");
			o = server.lock_write(id,client);
		}catch(RemoteException r){
		//	logger.fatal("Connexion lost, exception",r);
			r.printStackTrace();
		}finally{
			o = null;
		}
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
		so.lock();
		o = so.reduce_lock();	//objet inchangé	
		so.unlockLock();
		return o;
	}

	// receive a reader invalidation request from the server
	/**Method invalidate_reader : release RLT/RLC
	 *@param id : the id of the targeted object.
	**/
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		so.lock();
		so.invalidate_reader();
		so.unlockLock();
	}

	// receive a writer invalidation request from the server
	/**Method invalidate_writer : release WLC/WLT/WLC_RLT
	 *@param id : the id of the targeted object.
	 *@return o : the up-to-date object given back to the server
	**/
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		Object o;
		so.lock();
		o = so.invalidate_writer();
		so.unlockLock();
		return o;
	}	
}

