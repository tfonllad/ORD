import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	// Vérifier qu'on a le droit de rajouter un attribut. Je suis pas
	// certain des consignes
	private HashMap<String,int> localHMName;
	private HashMap<int,SharedObject_itf> localHMID;

	public Client() throws RemoteException {
		super();
		this.localHMName = new HashMapName<String,SharedObject_itf>;
	}
	

///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
	}
	
	// lookup in the name server
	public static SharedObject lookup(String name) {
		//appel sur serveur avec name1 :
		String name1 = name+this.localHMID.get(this.localHMName.get(name).toString());
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		//Enregistrement local		
		objID = this.localHMID.get(so);
		this.localHMName.put(name,objID); 

		//appel serveur .register( name+objID,objID)
		
	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		//communication avec le server, renvoit un id idObj
		
		//local
		sObj = new SharedObject();
		this.localHMID.put(idObj,sObj);		
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
