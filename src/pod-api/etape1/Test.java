public class Test{

	public static void main(String args[]){
		
		Client.init();
		
		SharedObject cpt = Client.lookup("COMPTEUR");
		if(cpt==null){
			cpt = Client.create(new Compteur());
			Client.register("COMPTEUR",cpt);
		}
		
		for(int i = 0; i < 50; i++){
			cpt.lock_read();
			((Compteur) cpt.obj).addOne();
			System.out.println("Valeur en cours :"+((Compteur)cpt.obj).get());
			cpt.unlock();
		}
		cpt.lock_read();
		System.out.println("Valeur finale :"+((Compteur)cpt.obj).get());
		cpt.unlock();
	}
}

