import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.HashMap;
import java.lang.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.tools.*;
import java.lang.reflect.*;

public class Client extends UnicastRemoteObject implements Client_itf {
	
	private static HashMap<Integer,SharedObject> hmID;
	private static Server_itf server;
	private static Client_itf client;	
	private static Logger logger = Logger.getLogger(Client.class.getName());	

	public Client() throws RemoteException {
		super();
	}

///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer -> OK
	public static void init() {
		logger.setLevel(Level.SEVERE);
        hmID = new HashMap<Integer,SharedObject>();
        StubGenerator stub = new StubGenerator();
		try{  	
			client = new Client();

			int port = 1099; 
			server = (Server_itf)Naming.lookup("//"+"localhost"+":"+String.valueOf(port)+"/Server");
			logger.log(Level.FINE,"Server found");
		}catch(Exception e){
			logger.log(Level.SEVERE,"Server not found");
			System.exit(-1);
		}	
	}

	// lookup in the name server
	/**Method lookup : get the Shared object from the server. If the object
 	* is not found , return a SharedObject with obj==null
	*@param name : name of registred object
	*@return so : the local SharedObject
	**/
	public static SharedObject lookup(String name){
		int id = 0;
		SharedObject so = null;	
        try{
            id = server.lookup(name);
        }catch(RemoteException e){
            logger.log(Level.SEVERE,"Server lost");
            System.exit(-1);
        }  
        // L'object exite-t-il localement?
        //
        if(hmID.containsKey(id)){
            so = hmID.get(id);
        }else{
        // On demande au server
            
            if(id==0){
                // le serveur ne connait pas l'objet
             }else{
                 // On récupère l'objet
                 Object o=null;
                 try{
                    o = server.lock_read(id,client);
                 // On récupère le stub.class
                 }catch(RemoteException e){}
                
                StubGenerator.generate_and_compile(o);   
                String class_name = o.getClass().getSimpleName()+"_stub";
                Class classe = null;
                try{
                    classe = Class.forName(class_name);
                    Constructor cons = classe.getConstructor(new Class[]{int.class});
                    so = (SharedObject) cons.newInstance(id);
                  }catch(NoSuchMethodException e){
                      e.printStackTrace();
                  }catch(InstantiationException e){
                      e.printStackTrace();
                  }catch(IllegalAccessException e){
                      e.printStackTrace();
                  }catch(InvocationTargetException e){
                      e.printStackTrace();
                  }catch(ClassNotFoundException e){
                      e.printStackTrace();
                  }
                hmID.put(id,so);
             }
        }
        return so;
	}		
	
	// binding in the name server
    /** Register a SharedObject under a specific name
     * @param name : the name of the SharedObject
     * @param so : the ServerObject
     */
	public static void register(String name, SharedObject_itf so) {
		int id =((SharedObject) so).getID();
		try{
			server.register(name,id);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}
	}

    // Dans cette étape, on cosidère que le stub existe et a été compilé.
    // On l'instencie en créant le SharedObject.

    public static SharedObject create_stub(int i, Object o){
        String class_name = o.getClass().getSimpleName()+"_stub";
        Class classe = null;
        try{
             classe = Class.forName(class_name);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }         
        // On récupère le constructeur et on instancie le stub
        Constructor cons = null; 
        SharedObject so = null;

        try{
         cons = classe.getDeclaredConstructor(new Class[]{int.class, Object.class});
         so = (SharedObject) cons.newInstance(i,o);
         }catch(NoSuchMethodException e){
            e.printStackTrace();
         }catch(InstantiationException e2){
             e2.printStackTrace();
         }catch(IllegalAccessException e3){
            e3.printStackTrace();
         }catch(InvocationTargetException e4){
            e4.printStackTrace();
         }
        return so;
    }
        
	// creation of a shared object
	/**Method create : give o to the server wich will deliver id and cache
 	* it. 
	* @param o : object to share.
	* @return so : local representation of the object.
	*/
	public static SharedObject create(Object o) {
		int id = -1;
		SharedObject so = null;
        
		try{
            //get an id from the server
			id = server.create(o);
            // generate and compile x_stub.java
            StubGenerator.generate_and_compile(o);
        //    try{
        //        Thread.sleep(100);
        //    }catch(InterruptedException e){}
            //create a local representation
			so = Client.create_stub(id,o);
			hmID.put(id,so);
				
	 	}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}
		return so;			
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	/** Method lock_read request a read lock from the server
	/*@param id : the id of the object to lock
	/*@return o : up-to-date object retrived from the serer
	**/
	public static Object lock_read(int id) {
		Object o = null;
		try{
			o = server.lock_read(id,client);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}
        if(o==null){
            logger.log(Level.SEVERE,"NULL object recovered on LR");
        }
		return o;	
	}

	/** Method lock_write request a write lock from the server
	*@param id : the id of the object to lock
	*@return o : up-to-date object retrived from the serer
	**/
	public static Object lock_write(int id) {
		Object o = null;
		try{
			o = server.lock_write(id,client);
		}catch(RemoteException r){
			logger.log(Level.SEVERE,"Connexion Lost");
			System.exit(-1);
		}
        if(o==null){
            logger.log(Level.SEVERE,"NULL object recovered on LW");
        }
		return o;
	}

	// receive a lock reduction request from the server
	
    /**Method reduce_lock : called on the client when he has WLC/WLT he will
	 * keep the right to read but loose WLC/WLT.
	 *@param id : the id of the targeted object.
	 *@return the up-to-date object given back to the server
	**/

	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		return hmID.get(id).reduce_lock();
	}


	// receive a reader invalidation request from the server
	
    /**Method invalidate_reader : release RLT/RLC
	 *@param id : the id of the targeted object.
	**/

	public void invalidate_reader(int id) throws java.rmi.RemoteException {
        if(hmID.containsValue(id)){ // lookup -> enregistrement lecteur alors que l'objet existe pas
            hmID.get(id).invalidate_reader();
        }
	}

	// receive a writer invalidation request from the server
    
	/**Method invalidate_writer : release WLC/WLT/WLC_RLT
	 *@param id : the id of the targeted object.
	 *@return o : the up-to-date object given back to the server
	**/
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {        
		return hmID.get(id).invalidate_writer();
	}	
}

