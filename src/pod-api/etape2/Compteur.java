import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.Random;

public class Compteur {
    private final static Logger LOGGER = Logger.getLogger(Compteur.class.getName());

     public static void main(String argv[]) {
        Client.init();
        LOGGER.setLevel(Level.INFO);
        SharedObject x;
        Random random = new Random();
        try{
            MyLogger.setup("Compteur_"+argv[1]);
        }catch(IOException e) {}
        if (Integer.parseInt(argv[0]) == -1) {
            x = Client.create(new Entier(0));
            Client.register("COMPTEUR", x);
            System.exit(0);
        }
        x = Client.lookup("COMPTEUR");
        if (x == null) {
            LOGGER.log(Level.SEVERE,"ERROR : Compteur devrait etre cree");
        }
        int i;
        int max = Integer.parseInt(argv[0]);
        for (i = 0; i < max; i++) {
            x.lock_write();
            LOGGER.log(Level.FINE,"Value : " + ((Entier) x.obj).getCompteur());
            ((Entier) x.obj).incr();
            x.unlock();

            x.lock_read();
            ((Entier)x.obj).getCompteur();
            x.unlock();
            }
        x.lock_read(); 
        LOGGER.log(Level.INFO,"ENDING : "+((Entier)x.obj).getCompteur());
        x.unlock(); 
        for(i=0;i<1000;i++) {
            x.lock_read();
            int l =  ((Entier)x.obj).getCompteur();
            x.unlock();
            int k = random.nextInt(i+l);
            if(k%2==0) {
                System.exit(0);
            }            
        }
    }
}

