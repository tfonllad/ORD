/**@version etape1
**/
import java.util.ArrayList;
import java.util.concurrent.locks.*;

public class ServerObject{

	private State lockState;
	private int id;
	private ArrayList<Client_itf> readerList; //contains no writers 
	private Client_itf writer;
	private Condition nI;
	
	/** Constructor ServerObject
	**/
	
	public ServerObject(int id){
		this.id = id;	
	}
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
		if(writer!=null){//a vérifier
			o = writer.invalidate_writer(this.id);
		}else{
			for(Client cli : readerList()){
				if(cli.getSharedObject(this.id).getLockState==State.RLT){
					c = cli;
					//break;
				}			
			}
			o = cli.getObject();
		}
		return o;
	}

	public Object reduce_lock(){
		Client c;
		Object o;
		//On récup_re l'écrivant
		if(writer!=null){
			o = writer.reduce_lock(this.id);
		}else{
			for(Client cli : readerList()){
				if(cli.getSharedObject(this.id).getLockState==State.RLT){
					c = cli;
					//break;
				}			
			}
			o = cli.getObject();
		}
	return o;	
	}
	
	public void invalidate_reader(){
		for(Client cli : readerList()){
			cli.invalidate_reader(this.id);		
		}
	}	
}	
