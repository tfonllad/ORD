import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int sobjID;
	
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
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
	}

	public synchronized Object invalidate_writer() {
	}
	
	//Getter
	public int getID(){
		return this.sobjID;
	}

	//Setter
	public void setID(int i){
		this.sobjID = i;
	}
}
