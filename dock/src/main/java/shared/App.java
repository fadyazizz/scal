package shared;

import com.Interfaces.Hook;
import com.rabbitmq.client.Delivery;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rabbitMQ.RabbitMQApp;
import rabbitMQ.RabbitMQCommunicatorApp;
import rabbitMQ.RabbitMQCommunicatorServer;
import rabbitMQ.RabbitMQServer;
import shared.classes.*;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public abstract class App  {

public Properties properties;
protected RabbitMQApp rabbitMQApp;
protected RabbitMQCommunicatorApp rabbitMQCommunicatorApp;
protected Controller appController;
protected ThreadPoolManager threadsManager;
public ClassManager classManager=new ClassManager();
public PostgresConnection sqlDb;

protected BackdoorServer backDoorServer=null;
protected static RabbitMQServer rabbitMQInterAppCommunication;

//read the .properties file and set the properties variable
public RabbitMQCommunicatorApp getRabbitMQCommunicatorApp(){
        return  this.rabbitMQCommunicatorApp;
    }
protected abstract String getAppName();

public  String loggingLevel="normal";
  
public void dbInit() throws IOException {
       
                if(properties.contains("arangodb")) {
                    System.out.println(" i created arango db !!!!");
                    Arango arango = Arango.getInstance();
                    arango.createPool(Integer.parseInt(this.properties.getProperty(AppsConstants.DEFAULT_NUMBER_OF_ARANGO_DB_PROPERTY_Name)));
                    arango.createDatabaseIfNotExists(properties.getProperty("arangodb"));
                }
                if(properties.contains("postgres")){
                    //TODO initialize postgres
                    System.out.println("I created postgres db");
                    sqlDb=new PostgresConnection();
                    sqlDb.setDbMaxConnections(this.properties.getProperty(AppsConstants.DEFAULT_NUMBER_OF_POSTGRES_DB_PROPERTY_Name));
                    sqlDb.initSource();
                }

    }

public  void start() throws IOException, TimeoutException, ClassNotFoundException {

       

    //Set<String> resourceList = this.getClass().getClassLoader().getResources(Pattern.compile(".*\\.properties"));
    //System.out.println(enumeration.count());

         
   
   System.out.println("resources");
      initProperties();
      this.dbInit();
      this.classManager.init();
      appController=new Controller(this);
   
    this.initRabbitMQ();
    
    this.threadsManager=new ThreadPoolManager();
    this.threadsManager.initThreadPool(10);
     
    appController.start();

    createBackDoorServer();
}

private void createBackDoorServer()  {
    new Thread(() -> {
        try {
            int portNumber=System.getenv("backDoorPort")==null?9090:Integer.parseInt(System.getenv("backDoorPort"));
            this.backDoorServer=new BackdoorServer(portNumber,appController);
            this.backDoorServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();

}
protected void initProperties() {
        this.properties=new Properties();
        try {
            
            this.properties.load(App.class.getClassLoader().getResourceAsStream("db.properties"));
          
            readDefaultProperties();
           
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
protected void readDefaultProperties(){
    String rabbitmqHost=System.getenv(AppsConstants.RabbitMQ_Host_PropertyName);
    this.properties.put(AppsConstants.RabbitMQ_Host_PropertyName,rabbitmqHost==null?AppsConstants.RABBITMQ_HOST_DEFAULT_VALUE:rabbitmqHost);

     String backDoorPort=System.getenv(AppsConstants.BACKDOOR_PORT_PROPERTY_NAME);
     this.properties.put(AppsConstants.BACKDOOR_PORT_PROPERTY_NAME,backDoorPort==null?AppsConstants.BACKDOOR_PORT_VALUE:Integer.parseInt(backDoorPort));


     this.properties.put(AppsConstants.DEFAULT_NUMBER_OF_ARANGO_DB_PROPERTY_Name,""+AppsConstants.DEFAULT_NUMBER_OF_ARANGO_DB_CONNECTIONS);
     this.properties.put(AppsConstants.DEFAULT_NUMBER_OF_POSTGRES_DB_PROPERTY_Name,""+AppsConstants.DEFAULT_NUMBER_OF_POSTGRES_DB_CONNECTIONS);
     
     this.properties.put(AppsConstants.DEFAULT_MAX_Threads_PROPERTY_NAME,""+AppsConstants.DEFAULT_MAX_THREADS_VALUE);

}
protected  void initRabbitMQ() throws IOException, TimeoutException {
    Hook hook=this::appHook;
    this.rabbitMQApp=new RabbitMQApp(properties.getProperty(AppsConstants.RabbitMQ_Host_PropertyName));
    this.rabbitMQCommunicatorApp=this.rabbitMQApp.getNewCommunicator(this.getAppName().toUpperCase()+"Server",hook);
    rabbitMQInterAppCommunication=new RabbitMQServer(properties.getProperty(AppsConstants.RabbitMQ_Host_PropertyName));
    
}
public void appHook(String consumerTag, Delivery delivery) throws IOException {
  //this method is called whenever this app's rabbitmq receives a message
   

    String response = "";
    
    try {
        // invoking command
        String req = new String(delivery.getBody(), StandardCharsets.UTF_8);

        

        // TODO change command name
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject propertiesJson = (org.json.simple.JSONObject) parser.parse(req);
        Object commandName = propertiesJson.get("commandName");

        response += invokeCommand((String) commandName, new JSONObject(req));

        
    } catch (ParseException e) {
        e.printStackTrace();
    } finally {
        // sending response message to the response queue, which is defined by the
        // request message's properties
             rabbitMQCommunicatorApp.sendResponse(delivery,response);

    }
}

    // function to invoke the required command using command pattern design
    public String invokeCommand(String commandName, JSONObject req) {
        
        final Callable<String> callable = () -> runCommand(commandName, req);
      
        // submitting the callback fn. to the thread pool
        Future<String> executorCallable = threadsManager.submit(callable);
        //here they are blocking ?? why !
        // waiting until the future is done
        
        while (!executorCallable.isDone()) ;

        // returning the output of the future
        try {
            return executorCallable.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "{\"statusCode\": 507, \"msg\": \"Execution was interrupted\"}";
        } catch (ExecutionException e) {
            System.out.println(e.getMessage());
            return "{\"statusCode\": 521, \"msg\": \"Execution threw an exception\"}";
        }
    }

    private String runCommand(String functionName, JSONObject req) {
        try {
            // getting the class responsible for the command
            // TODO get this from a classManager
           
            final Class<?> commandClass = classManager.getCommand(functionName);
            // creating an instance of the command class
          
            final Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();
            // callback responsible for invoking the required method of the command class
            commandInstance.validationProperties=classManager.validationMap.get(commandInstance.getCommandName());
            return (String) commandClass.getMethod("execute", req.getClass()).invoke(commandInstance, req);
        } 
        catch (ClassNotFoundException e) {
            
            return "{\"statusCode\": 404, \"msg\": \"Function-Name class: (" + functionName + ") not found\"}";
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            
            return "{\"statusCode\": 504, \"msg\": \"Function-Name class: not operational\"}";
        } catch (InvocationTargetException e) {
            
            e.printStackTrace();
            return "{\"statusCode\": 510, \"msg\": \"Function-Name class: threw an exception\"}";
        }
    }
public  ThreadPoolManager getThreadPool(){
    return threadsManager;
}



public static JSONObject communicateWithApp(String myAppName,String appToCommunicateWith,JSONObject originalRequest,String methodType,String commandName,JSONObject uriParams,JSONObject body){

        appToCommunicateWith=appToCommunicateWith.toUpperCase()+"Server";
        myAppName=myAppName.toUpperCase();
        String responseQueueName=myAppName+" InterAppCommunication "+appToCommunicateWith;

        JSONObject newRequestObject=new JSONObject(originalRequest.toString());
        newRequestObject.put("body",body==null?new JSONObject():body);
        newRequestObject.put("uriParams",uriParams==null?new JSONObject():uriParams);
        newRequestObject.put("methodType",methodType);
        JSONObject authenticated=new JSONObject();
        authenticated.put("isAuthenticated",true);
        newRequestObject.put("authenticationParams",authenticated);
        newRequestObject.put("commandName",commandName);

        
       
        try (RabbitMQCommunicatorServer channel = App.rabbitMQInterAppCommunication.getNewCommunicator()) {
            String response = channel.placeRequestInQueue(newRequestObject.toString(), appToCommunicateWith, responseQueueName);
            ByteBuf content = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8);
            return new JSONObject(response);
            
        } catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
            return null;
    }
    public static void sendMessageToApp(String appToCommunicateWith,JSONObject originalRequest,String methodType,String commandName,JSONObject uriParams,JSONObject body){

        appToCommunicateWith=appToCommunicateWith.toUpperCase()+"Server";
      
        

        JSONObject newRequestObject=new JSONObject(originalRequest.toString());
        newRequestObject.put("body",body==null?new JSONObject():body);
        newRequestObject.put("uriParams",uriParams==null?new JSONObject():uriParams);
        newRequestObject.put("methodType",methodType);
        
        newRequestObject.put("isAuthenticated",true);

        newRequestObject.put("commandName",commandName);



        try (RabbitMQCommunicatorServer channel = App.rabbitMQInterAppCommunication.getNewCommunicator()) {
            channel.call_withoutResponse(newRequestObject.toString(), appToCommunicateWith);
          
          

        } catch (IOException | TimeoutException  | NullPointerException e) {
            e.printStackTrace();
        }
       
    }


}


