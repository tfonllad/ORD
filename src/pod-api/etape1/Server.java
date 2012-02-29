/**@version etape1
* 
**/

public class Server implements Serveur_itf{

	private HashMap hmName <String,ServerObject>;
	private HashMap hmID <int,ServerObject>;

	/**Method lookup : return the ID of object "name" 
	* @param String
	* @return int
	* @throws RemoteException
	**/
	public int lookup(String name) throws java.rmi.RemoteException{
		try{
			int resID = hmID.get(this.hmName.get(name));	
		}catch(RemoteException r ){
			System.out.println(r.getMessage());
		}
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
		try{	
			ShardObject sObj = this.hmID.get(id);
			this.hmName.put(name+id.toString(),sObj);	
		}catch(RemoteException r ){
			System.out.println(r.getMessage());
		}
		return
	}
	
	/** Method create : create Server Object, add it to hmID and return ID
	* @param o : the object to create 
	* @return int : the ID
	* @throws RemoteException
	**/
	public int create(Object o) throws java.rmi.RemoteException{
		try{
			int objID =  o.hashcode(); //Faut-il désérialiser ?
			ServerObject sObj = new ServerObject();
			this.hmID.put(objID,sobj);	

		}catch(RemoteException r){
			System.out.println(r.getMessage();
		}
		return objID;
	}

}
