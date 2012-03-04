import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	

	//attribut lock

	private int id;
	public Object obj;
	
	public SharedObject(int id,Object object){
		this.id = id;
		this.obj = object;
	}
	// invoked by the user program on the client node
	public void lock_read() {
	}

	// invoked by the user program on the client node
	public void lock_write() {
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
	}

	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		return null;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {

	}

	public synchronized Object invalidate_writer() {
		return null;
	}

	public int getID(){
		return id;
	}
		
}
