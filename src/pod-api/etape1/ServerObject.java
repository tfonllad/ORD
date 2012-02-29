/**@version etape1
**/

public class ServerObject{

	private int lockState;
	private ArrayList<Client_if> clientList; // List of client who have up-to-date SharedObject 
	
	/** Constructor ServerObject
	**/
	public ServerObject(){	
	}

	int getLockState(){
		return this.lockState;
	}
}
