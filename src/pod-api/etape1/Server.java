/**@version etape1
* 
**/

public class Server implements Serveur_itf{

	private HashMap<String,ServerObject> hmName; 
	private HashMap<int,ServerObject> hmID ;
	
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
		int resID = hmID.get(this.hmName.get(name));	
		return resID;		
	} 
	
	/** Method Register : register the name and ID of a ServerObject
	* @param name : the name of the object. In fact name::ID because
	* different object from different client are allowed to have the same
	* name.
	* @param ID : the ID
	* @return void
	* @throws RemoteException
	**/
	public void register(String name,int id) throws java.rmi.RemoteException{
			ShardObject sObj = this.hmID.get(id);
			this.hmName.put(name,sObj);	
		return
	}
	
	/** Method create : create Server Object, add it to hmID and return ID
	* @param o : the object to create 
	* @return int : the ID
	* @throws RemoteException
	**/
	public int create(Object o) throws java.rmi.RemoteException{
			int objID =  o.hashcode(); //Faut-il désérialiser ?
			ServerObject sObj = new ServerObject(objID);
			this.hmID.put(objID,sobj);}
		return objID;
	}
	
	public void initialize(int id,Client_itf client) throws java.rmi.RemoteException{{
		this.hmID.get(id).addClient(client);
	}

	public static void main(String args[]){
		int port;
		String URL;
		try{
			Integer I = new Integer(args[0]);
			port = I.intValue();
			}catch(Exception e){
				System.out.println("Please enter:Server<port>");
				return
		}
		try{
			port = 1234;
			Registry registry = LocateRegistry.createRegistry(port);
			Server_itf server = new Server();
			URL =
"//"+InetAdress.getLocalHost().getHostName()+":"+port+"/Server";
			Name.rebind(URL,server);
		}catch(Exception e){
			System.out.println("Fail to initialize Server");
			e.printStackTrace();
		}				
	}
}
