public class Sentence_stub extends SharedObject implements Sentence_itf, java.io.Serializable {

	public Sentence_stub (int id, Object o){ 
		super(id,o);
	}
	public Sentence_stub (int id){ 
		super(id);
	}

	public void write(String arg0) {

		( (Sentence) obj ).write( arg0);
	}

	public String read() {

		return ( (Sentence) obj ).read();
	}

}