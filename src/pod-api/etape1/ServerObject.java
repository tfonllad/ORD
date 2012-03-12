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
	}
	
	public Object invalidate_writer(){
		Client c;
		Object o;
		// on récupère le client : on doit vérifier que c'est un
		// ecrivain
		if(writer!=null){//a vérifier
			o = writer.invalidate_writer(this.id);
			writer = null;
		}else{
			for(Client_itf cli : readerList){
				if(((Client)cli).getSharedObject(this.id).getLockState()==State.RLT){
					c = (Client)cli;
					//break;
				}			
			}
			o = c.getObject(this.id);
		}
		return o;
	}
	public Object lock_read(Client_itf client){
		Object o;
		o = this.reduce_lock();	
		readerList.add(client);
		return o;
	}
	public Object reduce_lock(){
		Client c;
		Object o;
		//On récup_re l'écrivant
		if(writer!=null){
			o = writer.reduce_lock(this.id);
			this.readerList.add(this.writer);
			writer = null;
		}else{
			for(Client_itf cli : readerList){
				if(((Client)cli).getSharedObject(this.id).getLockState()==State.RLT){
					c = (Client) cli;
					//break;
				}			
			}
			o = c.getObject(this.id);
		}
	return o;	
	}
	
	public void invalidate_reader(){
		for(Client_itf cli : readerList){
			cli.invalidate_reader(this.id);		
			this.readerList.remove(cli);
		}
	}	
}	
