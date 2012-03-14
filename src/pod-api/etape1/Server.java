/**@version etape1
* 
**/
import java.util.HashMap;
import java.rmi.registry.*;
import java.rmi.*;
import java.net.*;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements Server_itf{

	private HashMap<String,Integer> hmName; 
	private HashMap<Integer,ServerObject> hmID;
	private static int cpt; 				//generation of id
	
	public Server() throws RemoteException{
		super();
		this.hmName = new HashMap<String,Integer>();
		this.hmID = new HashMap<Integer,ServerObject>();
		this.cpt = 0;
	}
	public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException{	
		ServerObject so = this.hmID.get(id);
		so.lock_read(client);
		return so.obj;
	}

        public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException{
		System.out.println("Propagation de lock_write");
		ServerObject so = this.hmID.get(id);
		so.lock_write(client);
		return so.obj;
	}

 	/** Method Shared Object : called when client lookup an object.
	* @param id : id of the object
	* @return obj : from cache
	**/

	public Object getObject(int id) throws java.rmi.RemoteException{
		return this.hmID.get(id).obj; 
	}
		

	/**Method lookup : return the ID of object "name" if it was registered,
 	* otherwise return id = 0
	* @param name
	* @return id
	* @throws RemoteException
	**/
	public int lookup(String name) throws java.rmi.RemoteException{
		int id;
		System.out.println("lookup");
		if(!this.hmName.containsKey(name)){
			System.out.println("Name not found");
			id = 0;
		}else{
			System.out.println("Name found");	
			id = this.hmName.get(name);
		}
		return id;		
	} 
	
	/** Method Register : register the name and ID of a ServerObject
	* @param name : the name of the object
	* @param ID : the ID
	* @return void
	* @throws RemoteException
	**/
	public void register(String name,int id) throws java.rmi.RemoteException{
			System.out.println("Register");
			ServerObject so = this.hmID.get(id);
			if(!hmName.containsKey(name)){
				this.hmName.put(name,id);
			}else{ 	/* name already bound to another object */
			 	/* lancer une exception rmi ou  ne rien faire */
				System.out.println("Le server possède déjà lenom");
			}
	}

	/** Method create : create Server Object, add it to hmID and return ID
	* @param o : the object to create 
	* @return cpt : the ID
	* @throws RemoteException
	**/
	public int create(Object o) throws java.rmi.RemoteException{
		cpt = cpt+1;
		ServerObject so = new ServerObject(cpt,o);
		this.hmID.put(cpt,so);
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
			System.out.println("Fail to initialize Server");
			e.printStackTrace();
		}				
	}
}
