/**@version etape1
* 
**/
import java.util.HashMap;
import java.rmi.registry.*;
import java.rmi.*;
import java.net.*;

public class Server implements Server_itf{

	private HashMap<String,ServerObject> hmName; 
	private HashMap<Integer,ServerObject> hmID;
	private int cpt; 				//generation of id

	public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException{	
		ServerObject so = this.hmID.get(id);
		Object o;
		so.lock();
		o = so.lock_read(client);		
		so.unlock();
		return o;
	}

        public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException{
		//meme shéma qu'au dessus : attente bloquant sur initialisation,
		//e bloquante sur le droit d'écriture
		//après avoir le droit d'écriture, aller invalider
		ServerObject so = this.hmID.getID();
		Object o;
		so.lock();
		o = so.lock_write(client);
		so.lock();
		return o;
	}

 	/** Method Shared Object : Called when Client1 lookup(obj) and the server
 	* has to take this obj from Client2
	* @param id : id of the object
	* @return SharedObject from client2
	**/

	public Object getObject(int id) throws java.rmi.RemoteException{
		return this.hmID.get(id).obj; 
	}
		

	/**Method lookup : return the ID of object "name" if it was registered, otherwise return null
	* @param String
	* @return int
	* @throws RemoteException
	**/
	public int lookup(String name) throws java.rmi.RemoteException{
		int resID;
		ServerObject sObj = this.hmName.get(name);
		if (sObj==null){
			//valeur de id caractéristique de l'abscence de l'objet.
			resID=0;
		}
		else{
			resID = sObj.getID();
		}
		sObj.lock();
		while(!sObj.isINI()){
			try{
				sObj.awaitINI();	
			}catch(InterruptedException i){
			}
		}
		sObj.signalINI();
		sObj.unlock();
		return resID;		
	} 
	
	/** Method Register : register the name and ID of a ServerObject
	* @param name : the name of the object
	* @param ID : the ID
	* @return void
	* @throws RemoteException
	**/
	public void register(String name,int id) throws java.rmi.RemoteException{
			ServerObject so = this.hmID.get(id);
			this.hmName.put(name,so);
			this.hmID.get(id).setINI();
			this.hmID.get(id).signalINI;
	}
	
	/** Method create : create Server Object, add it to hmID and return ID
	* @param o : the object to create 
	* @return int : the ID
	* @throws RemoteException
	**/
	public int create(Object o) throws java.rmi.RemoteException{
		int id =  cpt;
		cpt = cpt+1;
		ServerObject so = new ServerObject(id,o);
		this.hmID.put(id,so);
		return id;
	}
	
	/**Method initialize : add the client to the list of client disposing of
 	* up-to-date SharedObject. The Client here did call create.
	* @param id : id of the ServerObject
	* @param client : Client_itf identify the client
	* @return void
	**/
	public void initialize(int id,Client_itf client) throws java.rmi.RemoteException{	
		this.hmID.get(id).addClient(clientR);
	}

	public static void main(String args[]){
		int port;
		String url;
		Registry registry;
		cpt = 0;
		Integer I = new Integer(args[0]);
		Server_itf server = new Server();
		
		try{
			port = 1099;
			registry = LocateRegistry.createRegistry(port);
			url ="//"+InetAddress.getLocalHost().getHostName()+":"+port+"/Server";
			System.out.println(url);
			Naming.rebind(url,server);
		}catch(Exception e){
			System.out.println("Fail to initialize Server");
			e.printStackTrace();
		}				
	}
}
