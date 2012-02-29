/**@version etape1
**/

public class ServerObject{

	private int lockState;
	private ArrayList<Client_if> clientList; // List of client who have up-to-date SharedObject 
	
	/** Constructor ServerObject
	*@param objID : the ID of the object to share
	**/
	public ServerObject(){
		this.lockState = 0;	
	}

	int getLockState(){
		return this.lockState;
	}
}
