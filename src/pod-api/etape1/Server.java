/**@version etape1
* 
**/
import java.util.HashMap;
import java.rmi.registry.*;
import java.rmi.*;
import java.net.*;

public class Server implements Server_itf{

	private HashMap<String,Integer> hmName; 
	private HashMap<Integer,ServerObject> hmID;
	private static int cpt; 				//generation of id

	public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException{	
		ServerObject so = this.hmID.get(id);
		Object o;
		so.lock();
		o = so.lock_read(client);		
		so.unlock();
		return o;
	}

        public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException{
		ServerObject so = this.hmID.getID();
		Object o;
		so.lock();
		o = so.lock_write((Client)client);
		so.lock();
		return o;
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
		int id = this.hmName.get(name);
		ServerObject so = this.hmID.get(id);
		if (so==null){
			id=0; // id = 0 <=> object not found
		}
		else{
			id = so.getID();
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
			ServerObject so = this.hmID.get(id);
			if(!hmName.containsKey(name)){
				this.hmName.put(name,id);
			}else{ 	/* name already bound to another object */
			 	/* lancer une exception rmi ou  ne rien faire */
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
