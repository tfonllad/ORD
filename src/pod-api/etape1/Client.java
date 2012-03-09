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
			server = (Server) Naming.lookup("//"+host+":"+port+"/Server");
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
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
		so = hmID.get(id);
		so.lock();
		Object o;
		while(so.isReadable()){
			try{
				so.takeLock();//c'est un await
			}catch(InterruptedException i){}
		}
		switch so.getLockState(){

			case NL :
			o = server.lock_read(id,client);
			so.updateLock(State.RLT);
			break;
			
			case RLC:
			o = server.lock_read(id,client);
			so.updateLock(State.RLT);
			break;
		
			case WLC:
			o = server.lock_read(id,client);
			so.updateLock(State RLT_WLC);
			break;
		}
		so.unlock();
		return o;
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
		so = hmID.get(id);
		so.lock();
		Object o;
		while(so.isWritable()){
			try{
				so.takeLock(); // c'est un await
			}catch(InterruptedException i){
			}
		}
		switch so.getLockState(){

			case NL:
			o = server.lock_write(id,client);
			so.updateLock(State.WLT);
			break;

			case RLC:
			o = server.lock_write(id,client);
			so.updateLock(State.WLT);
			break;

			case WLC:
			o = server.lock_write(id,client);	
			break;
		}

		so.unlock();
		return o;
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		so.lock();
		Object o = so.obj;
		//les release sont des signal
		switch so.getLockState(){
			case RLT_WLC:
			so.updateLock(State.RLT);
			so.releaseLock();
			break;
			case WLC:
			so.updateLock(State.RLC);
			so.releaseLock();
			break;
			case WLT:
			so.updateLock(State.RLC);
			so.releaseLock();
			break;
		}
		
		so.unlock():
		return o;
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		so.lock();
		// les release sont des signals
		switch so.getLockState(){
			case RLC:
			so.updateLock(State.NL);
			so.releaseLock();
			break;
			case RLT:
			so.updateLock(State.NL);
			so.releaseLock();
			break;
		}
		so.unlock();
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		SharedObject so = hmID.get(id);
		Object o = so.obj;
		// les release sont des signals
		//updateLock on SharedObject
		so.lock();
		switch so.getLockState(){
			case WLT :
				so.updateLock(NL);
				so.releaseLock(); 	
			break;
			case WLC:
				so.updateLock(NL);
				so.releaseLock();
			break;
			case RLT_WLC:
				so.updateLock(NL);
				so.releaseLock();
			break;
		}
		so.unlock();
		return o;
	}
}
