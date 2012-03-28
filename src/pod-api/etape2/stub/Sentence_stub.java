public class Sentence_stub extends SharedObject, implements Sentence_itf, java.io.Serializable {

	public Sentence_stub (int id, Object o){ 
		super(id,o);
	}

	public void write(String arg0) {
		(Sentence)object = (Sentence).obj ;
		object.write( arg0);
	}

	public String read() {
		(Sentence)object = (Sentence).obj ;
		return object.read();
	}

	public void wait(long arg0, int arg1) throws InterruptedException{
		(Sentence)object = (Sentence).obj ;
		object.wait( arg0,  arg1);
	}

	public void wait(long arg0) throws InterruptedException{
		(Sentence)object = (Sentence).obj ;
		object.wait( arg0);
	}

	public void wait() throws InterruptedException{
		(Sentence)object = (Sentence).obj ;
		object.wait();
	}

	public boolean equals(Object arg0) {
		(Sentence)object = (Sentence).obj ;
		return object.equals( arg0);
	}

	public String toString() {
		(Sentence)object = (Sentence).obj ;
		return object.toString();
	}

	public int hashCode() {
		(Sentence)object = (Sentence).obj ;
		return object.hashCode();
	}

	public Class getClass() {
		(Sentence)object = (Sentence).obj ;
		return object.getClass();
	}

	public void notify() {
		(Sentence)object = (Sentence).obj ;
		object.notify();
	}

	public void notifyAll() {
		(Sentence)object = (Sentence).obj ;
		object.notifyAll();
	}

}