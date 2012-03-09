/**@version etape1
**/
import java.util.ArrayList;
import java.util.concurrent.locks.*;

public class ServerObject{

	private State lockState;
	private int id;
	private ArrayList<Client_itf> clientList; // List of client who have up-to-date SharedObject 
	private Condition nI;
	
	/** Constructor ServerObject
	**/
	
	public ServerObject(int id){
		this.id = id;	

	/** Methode updateLock is called after waiting process get out the await
 	* loop.
	* @param verrou
	* @return void
	**/
	public void updateLock(State verrou){
		switch(verrou){
			case NI:
				this.lockState = State.NI;
			break;
			case NL:
				this.lockState = State.NL;
			break;
			case RL:
				this.lockState = State.RL;
			break;
			case WL:
				this.lockState = State.WL;
			break;
		}
	}

	public int getID(){
		return this.id;
	}
	public synchronized State getLockState(){
		return this.lockState;
	
	public Object invalidate_writer(){
		Client c;
		Object o;
		// on récupère le client : on doit vérifier que c'est un
		// ecrivain
		o = c.invalidate_writer(this.id);
		return o;
	}
	public Object reduce_lock(){
		Client c;
		Object o;
		//On récup_re l'écrivant
		o = c.reduce_lock(this.id);
		return o;
}	
