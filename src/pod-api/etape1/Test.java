import java.lang.Thread;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

public class Test{
private static Test test;
private static Logger LOGGER = Logger.getLogger(Test.class.getName());
	
	public void writeLog(){
		LOGGER.setLevel(Level.INFO);
	}
		
	public static void main(String args[]){			
		String name = args[0];
		Test logger = new Test();
		try{
			MyLogger.setup(name);

		}catch(IOException e){}	
		logger.writeLog();	
	 		
		Client.init();
		int i=0;

		Random r = new Random();
		SharedObject cpt = Client.lookup("COMPTEUR");
		
		if(cpt==null){
			cpt = Client.create(new Compteur());
			Client.register("COMPTEUR",cpt);
			LOGGER.log(Level.INFO,"Création de Compteur");
		}else{
			LOGGER.log(Level.INFO,"Récupération de Compteur");
		}
		if(name.equals("1")){
			LOGGER.log(Level.INFO,"Lancer les autres process !");			
			try{
				Thread.sleep(2000);
			}catch(InterruptedException t){}
	
			for(int k=0;k<200;k++){
				cpt.lock_write();
				((Compteur) cpt.obj).addOne();
				i = ((Compteur) cpt.obj).get();
				int l = k + 1;
				LOGGER.log(Level.INFO,"Client"+name+" ecrit "+i+".");
				cpt.unlock();
				try{
					Thread.sleep(r.nextInt(3));
				}catch(InterruptedException t){}
			}
			System.out.println(name+" final : "+i+".");
			//System.exit(0);
		}else{		
		try{
			Thread.sleep(3000);
			}catch(InterruptedException t){}
			for(int k=0;k<200;k++){
			
				cpt.lock_read();
				i = ((Compteur)cpt.obj).get();
				LOGGER.log(Level.INFO,name+" a lu :"+i+".");
				cpt.unlock();
			try{
					Thread.sleep(r.nextInt(1));
				}catch(InterruptedException t){}
				cpt.lock_write();
				((Compteur) cpt.obj).addOne();
				i = ((Compteur) cpt.obj).get();
				int l = k+1;
				LOGGER.log(Level.INFO,"Client"+name+" ecrit "+i+".");
				cpt.unlock();
			}
			LOGGER.log(Level.FINE,"Client"+name+", final : "+i+".");
		}
		/*	
		while(!test.end(cpt)){
			try{
				Thread.sleep(100);
			}catch(InterruptedException t){}
		}
			
		cpt.lock_read();
		i = ((Compteur)cpt.obj).get();
		cpt.unlock();
		LOGGER.log(Level.SEVERE,"Client"+name+" a quitté avec :"+i+".");
		System.exit(0);*/
	}	
	public static boolean end(SharedObject so){
		so.lock_read();
		int i = ((Compteur)so.obj).get();
		so.unlock();
		return i == 6000;
	}
}
