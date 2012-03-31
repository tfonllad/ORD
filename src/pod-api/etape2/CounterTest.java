
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author fabien
 */
public class CounterTest {
    public static void main(String[] args) {
        int value = 0;
        int max   = -1;
        if (args.length == 1) {
            try {
                max = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                max = -1;
            }
        }
        if (max < 0) {
            System.out.println("Usage: java CounterTest <nb_increment>");
            System.exit(1);
        }

        Client.init();
        SharedObject counter = Client.lookup("Counter");
        if (counter == null) {
            counter = Client.create(new Counter());
            Client.register("Counter", counter);
        }

        for (int i = 0; i < max; i++) {
			try{
				Thread.sleep(1000);
			}catch(InterruptedException t){}
			
            counter.lock_write();
            ((Counter)counter.obj).inc();
            counter.unlock();

			try{
				Thread.sleep(1000);
			}catch(InterruptedException t){}
            
            counter.lock_read();
            value = ((Counter)counter.obj).get();
            counter.unlock();
            System.out.print(String.format("\r%1$10d ",value));
        }

        counter.lock_read();
        value = ((Counter)counter.obj).get();
        counter.unlock();
        System.out.println(String.format("\r%1$10d ",value));
        System.exit(0);
    }

    private static class Counter implements Serializable {
        private int c = 0;
        void inc() { c++; }
        int get() { return c; }
    }
}
