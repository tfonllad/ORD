
public class Compteur implements java.io.Serializable{
	private int cpt;
	public Compteur(){
		this.cpt = 0;
	}
	public int get(){
		return this.cpt;
	}
	public void addOne(){
		this.cpt = this.cpt+1;
	}
}
