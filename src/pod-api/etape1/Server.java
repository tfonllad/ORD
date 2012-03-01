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
		ServerObject sObj = this.hmName.get(name);
		int resID = sObj.getID();
		sObj.lockNI();
		while(sObj.getLockState()==Lock.NI){
			try{
				sObj.awaitNI();	
			}catch(InterruptedException){
			}
		}
			//Everyone is now allowed to lookup;
			sObj.signalNI();
		sObj.unlockNI();
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
			int objID =  o.hashcode();
			ServerObject sObj = new ServerObject(objID,Lock.NI);

			this.hmID.put(objID,sobj);}
		return objID;
	}
	
	/**Method initialize : add the client to the list of client disposing of
 	* up-to-date SharedObject. The Client here did call create.
	* @param id : id of the ServerObject
	* @param client : Client_itf identify the client
	* @return void
	**/
	public void initialize(int id,Client_itf client) throws java.rmi.RemoteException{
		this.hmID.get(id).addClient(client);
		this.hmID.get(id).signalID();
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
