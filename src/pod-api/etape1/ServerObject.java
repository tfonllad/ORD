/**@version etape1
**/

public class ServerObject{

	private int lockState;
	private int id;
	private ArrayList<Client_itf> clientList; // List of client who have up-to-date SharedObject 
		
	/** Constructor ServerObject
	**/
	public ServerObject(int id){
		this.id = id;	
	}

	int getLockState(){
		return this.lockState;
	}
	public Client_itf getClient(){
		return this.clientList.get(0);
	}
	public void addClient(Client_itf c){
		this.clientList.add(c);
	}
}
