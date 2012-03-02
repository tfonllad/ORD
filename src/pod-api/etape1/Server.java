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

	}

        public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException{
	}

 	
	public SharedObject getSharedObject(int id) throws java.rmi.RemoteException{
		ServerObject serverObject = this.hmID.get(id);
		Client_itf client = serverObject.getClient();
		return client.getSharedObject(id);
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
	public void initialize(int id,Client_itf client) throws java.rmi.RemoteException{
		this.hmID.get(id).addClient(client);
		this.hmID.get(id).signal(State.NI);
	}

	public static void main(String args[]){
		int port;
		String url;
		try{
			Integer I = new Integer(args[0]);
			port = I.intValue();
		}catch(Exception e){
			System.out.println("Please enter:Server<port>");
		}
		try{
			port = 1234;
			Registry registry = LocateRegistry.createRegistry(port);
			Server_itf server = new Server();
			url ="//"+InetAddress.getLocalHost().getHostName()+":"+port+"/Server";
			Name.rebind(url,server);
		}catch(Exception e){
			System.out.println("Fail to initialize Server");
			e.printStackTrace();
		}				
	}
}
