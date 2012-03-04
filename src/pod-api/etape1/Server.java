/**@version etape1
* 
**/
import java.util.HashMap;
import java.rmi.registry.*;
import java.rmi.*;
import java.net.*;
public class Server implements Server_itf{

	private HashMap<String,ServerObject> hmName; 
	private HashMap<Integer,ServerObject> hmID ;

	public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException{	
		return null;
	}

        public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException{
		return null;
	}

 	/**Method Shared Object : Called when Client1 lookup(obj) and the server
 	* has to take this obj from Client2
	* @param id : id of the object
	* @return SharedObject from client2
	**/
	// On pourrait aussi renvoyer l'objet directement ?

	public Object getSharedObject(int id) throws java.rmi.RemoteException{
		ServerObject serverObject = this.hmID.get(id);
		Client client = serverObject.getClient();
		return client.getSharedObject(id).obj; 
	}
		

	/**Method lookup : return the ID of object "name" 
	* @param String
	* @return int
	* @throws RemoteException
	**/
	public int lookup(String name) throws java.rmi.RemoteException{
		ServerObject sObj = this.hmName.get(name);
		int resID = sObj.getID();
		sObj.lock();
		while(!sObj.getLockState().equals(State.NI)){
			try{
				sObj.await(State.NI);	
			}catch(InterruptedException i){
			}
		}
			//Everyone is now allowed to lookup;
		sObj.signal(State.NI);
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
	}
	
	/** Method create : create Server Object, add it to hmID and return ID
	* @param o : the object to create 
	* @return int : the ID
	* @throws RemoteException
	**/
	public int create(Object o) throws java.rmi.RemoteException{
		int id =  o.hashCode();
		ServerObject so = new ServerObject(id);
		this.hmID.put(id,so);
		return id;
	}
	
	/**Method initialize : add the client to the list of client disposing of
 	* up-to-date SharedObject. The Client here did call create.
	* @param id : id of the ServerObject
	* @param client : Client_itf identify the client
	* @return void
	**/
	/* Le problème dans cette méthode est de lier avec rmi le client au
 * server de façon à ce qu'on puisse identifier le client et appeler des
 * méthodes dessus. Donc faut faire du naming.lookup(String name) mais pour
 * l'instant je ne sais pas comment ca marche exactement, "name" doit pas être
 * donnée n'importe comment, le client doit bien s'enregistre quelque part avec
 * la bonne adresse. Donc faut regarder le cours et la javadoc. Cela dit, ca
 * C'est la même chose pour les locks*/

	public void initialize(int id,Client_itf client,String name) throws java.rmi.RemoteException{
		Client clientR; 
		try{
 			clientR = (Client) Naming.lookup(name);
		}catch(NotBoundException e){
			System.out.println("server.initialize NBE");
			e.printStackTrace();
		}finally{
			clientR = null;
		}
		this.hmID.get(id).addClient(clientR);
		this.hmID.get(id).updateLock(State.NL);
		this.hmID.get(id).signal(State.NI);
		
	}
	//public int getClientID(Client client){
	//	return client.hashCode();
	//}
	public static void main(String args[]){
		int port;
		String url;
		Registry registry;
		Integer I = new Integer(args[0]);
		Server_itf server = new Server();
		
		try{
			port = I.intValue();
		}catch(Exception e){
			System.out.println("Please enter:Server<port>");
		}

		try{
			port = 1099;
			registry = LocateRegistry.createRegistry(port);
			url ="//"+InetAddress.getLocalHost().getHostName()+":"+port+"/Server";
			Naming.rebind(url,server);
		}catch(Exception e){
			System.out.println("Fail to initialize Server");
			e.printStackTrace();
		}				
	}
}
