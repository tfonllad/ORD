import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.HashMap;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	// Vérifier qu'on a le droit de rajouter un attribut. Je suis pas
	// certain des consignes
	
	private static HashMap<Integer,SharedObject> localHMID;
	private static Server server;
	private static Client client;	

	public SharedObject getSharedObject(int id){
		return this.localHMID.get(id);
	}

	public Client() throws RemoteException {
		super();
		this.localHMID = new HashMap<Integer,SharedObject>();
	}
	

///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
		this.localHMID = new HashMap<Integer,SharedObject>();
		client = new Client();
		String URL;
		//Connexion
		try{ 	
			int port = 1234;
			URL="//"+InetAddress.getLocalHost().getHostName()+":"+port+"/Server"; 
			Server serverRes =  Naming.lookup(URL);
			server = serverRes;

		}catch(Exception e){
			System.out.println("Faild to connect to the Server");
			e.printStackTrace();
		}
	}
	
	// lookup in the name server

	public static SharedObject lookup(String name){
		try{
			int id = server.lookup(name);
			SharedObject sobj = newSharedObject(id,server.getSharedObject(id).obj);
			return sobj;

		}catch(RemoteException r){
		}			
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
		try{			
			int id = server.create(o);		
			server.initialize(id,client);
			SharedObject sObj = new SharedObject(id,o);
			localHMID.put(id,sObj);	
	 
		}catch(RemoteException r){
			r.printStackTrace();
		}		
		return sObj;
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
	}
}
