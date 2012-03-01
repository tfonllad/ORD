import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	// Vérifier qu'on a le droit de rajouter un attribut. Je suis pas
	// certain des consignes
	
	private HashMap<int,SharedObject_itf> localHMID;
	private Server_itf server;
	
	public SharedObject getSharedObject(int id){
		return this.get(id);
	}

	public Client() throws RemoteException {
		super();
		this.localHMName = new HashMapName<String,SharedObject_itf>;
	}
	

///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
		this.localHMID = new HashMap<int,SharedObject_itf>();
		String URL;
		//Connexion
		try{ 	
			int port = 1234;
			URL="//"+InetAdress.getLocalHost().getHostName()+":"+port+"/Server"; 
			Server_itf server = (Server_itf) Naming.lookup(URL);
			this.server = server;

		}catch(Exception e){
			System.out.println("Faild to connect to the Server");
			e.printStackTrace();
		}
	}
	
	// lookup in the name server

	public static SharedObject lookup(String name){
		try{
			int idObj = this.server.lookup(name);
			SharedObject sobj = newSharedObject(id,this.server.getShardObject(id).object);
			return sobj;

		}catch(RemoteException r){
		}
					
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		//Enregistrement local		
		int objID = this.localHMID.get(so);

		try{
			this.server.register(name,objID);
		}catch(RemoteException r){
			r.printStackTrace();
		}

	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		//communication avec le server, renvoit un id idObj
		try{
			int idObj = this.server.create(o);		
			this.server.initialize(idObj);	
		}catch(RemoteException r){
			r.printStackTrace();
		}
		sObj = new SharedObject(idObj,o);
		this.localHMID.put(idObj,sObj);		
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
