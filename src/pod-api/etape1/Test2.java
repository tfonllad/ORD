import java.lang.Thread;
import java.util.Random;
public class Test2{
private static Test2 test;
	public static void main(String args[]){		
		
		test = new Test2();

		Client.init();
		int i=0;
		String name = args[0];
		Random r = new Random();
		SharedObject cpt = Client.lookup("COMPTEUR");
		
		if(cpt==null){
			cpt = Client.create(new Compteur());
			Client.register("COMPTEUR",cpt);
			System.out.println("Création de Compteur");
		}else{
			System.out.println("Récupération de Compteur");
		}
		if(name.equals("1")){
			System.out.println("Lancer les autres process !");			
			try{
				Thread.sleep(3000);
			}catch(InterruptedException t){}
	
			for(int k=0;k<50;k++){
				cpt.lock_write();
				((Compteur) cpt.obj).addOne();
				i = ((Compteur) cpt.obj).get();
				System.out.println(name+" a ecrit :"+i+".");
				cpt.unlock();
				try{
					Thread.sleep(r.nextInt(1)*62);
				}catch(InterruptedException t){}
			}
			System.out.println(name+" final : "+i+".");
			//System.exit(0);
		}else{		
			for(int k=0;k<50;k++){
				cpt.lock_read();
				i = ((Compteur)cpt.obj).get();
				System.out.println(name+" a lu :"+i+".");
				cpt.unlock();
			try{
					Thread.sleep(r.nextInt(1)*40);
				}catch(InterruptedException t){}
				cpt.lock_write();
				((Compteur) cpt.obj).addOne();
				i = ((Compteur) cpt.obj).get();
				System.out.println(name+" a ecrit :"+i+".");
				cpt.unlock();
			}
			System.out.println(name+" final : "+i+".");
		}	
		while(!test.end(cpt)){
			try{
				Thread.sleep(100);
			}catch(InterruptedException t){}
		}
		System.out.println("Fin");
		System.exit(0);
	}	
	public static boolean end(SharedObject so){
		so.lock_read();
		int i = ((Compteur)so.obj).get();
		so.unlock();
		return i == 300;
	}
}
