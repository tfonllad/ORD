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
	//private static int idClient;

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
		String serverURL;
		//String clientURL;
		//int idClient;
		//Connexion
		try{  	client = new Client();	
			port = 1099;
			serverURL="????";  
			server = (Server) Naming.lookup(url);
			//getRegistry(host)
			//enregistrer le client pour que le serveur fasse des
			//appels dessus. Pour cela il faut identifier de manière
			//unique le client, et donc que le serveur lui donne un
			//ID unique. On peut envisager un compteur dans serveur,
			//je sais pas is HashCode peut fonctionner
			//idClient = server.getClientID(client);
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
			server.initialize(id,client,"Client");
			so = new SharedObject(id,o);
			localHMID.put(id,so);	
	 	}catch(RemoteException r){
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
	public static Object lock_read(int id) {
		return null;
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
		return null;
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		return null;
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		return null;
	}
}
