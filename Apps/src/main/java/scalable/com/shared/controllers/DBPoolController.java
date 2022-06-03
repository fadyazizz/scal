package scalable.com.shared.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class DBPoolController {
    //Hashmap contains required databse(s) for this app to connect to
    private HashMap<String,DBConfigure> requiredDB;

    public DBPoolController(HashMap<String,DBConfigure> requiredDB)  {
        this.requiredDB = requiredDB;
    }

    
    public void initRequiredDBClasses () throws ReflectiveOperationException {
        Set<String> requiredDBClassNames = requiredDB.keySet();
        for(final String dbClassName : requiredDBClassNames){
            final DBConfigure dbConfig = requiredDB.get(dbClassName);
            System.out.println("DB className: " + dbClassName);

            final Class<?> cls = Class.forName(dbClassName);
            System.out.println("DB class: " + cls);
            //TODO: Add method name to a properties file
           DatabaseClient clientInstance = (DatabaseClient) cls.getMethod("getName").invoke(null);
            System.out.println("Client Instance: " + clientInstance);
            dbConfig.setMyClient(clientInstance);
        }
        
    }
    public void initRequiredDataBasePool(){
        Collection<DBConfigure> requiredDBConfiguration = requiredDB.values();
        for(final DBConfigure dbFConfig: requiredDBConfiguration){
            final DatabaseClient client = dbFConfig.getMyClient();
            final int maxConnections= client.getMaxConnectionCount();
            client.createPool(maxConnections);
        }
    }

    public void releaseDBPools(){
        for(DBConfigure dbConfig: requiredDB.values()){
            try {
                dbConfig.getMyClient().destroyPool();
            }
            catch(ClientDoesNotExistException e) {
                e.printStackTrace();
            }

        }
    }

    //TODO: check if needed add set max connection for all and set max connection for a specific client or not



}
