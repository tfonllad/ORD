import java.util.Random;
import java.lang.Thread;
public class Test{

	public static void main(String args[]){
		
		Client.init();
		Random r = new Random();
		SharedObject cpt = Client.lookup("COMPTEUR");
		if(cpt==null){
			cpt = Client.create(new Compteur());
			Client.register("COMPTEUR",cpt);
			System.out.println("Création de Compteur");
		}else{
			System.out.println("Récupération de Compteur");
		}
		
		for(int i = 0; i < 50; i++){
			cpt.lock_write();
			((Compteur) cpt.obj).addOne();
			System.out.println("Valeur en cours :"+((Compteur)cpt.obj).get());
			cpt.unlock();
				int j = r.nextInt(i+1);
			try{
				Thread.sleep(j*100);
			}catch(InterruptedException t){}
		}
		cpt.lock_read();
		System.out.println("Valeur finale :"+((Compteur)cpt.obj).get());
		cpt.unlock();
	}
}

