package scalable.com.shared.classes;

import com.fasterxml.jackson.core.JsonParser;
import org.apache.ibatis.ognl.ObjectElementsAccessor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scalable.com.exceptions.ValidationException;
import scalable.com.shared.App;
import scalable.com.shared.AppsConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.*;

public class Controller  {

App app;

public Controller(App app){
    this.app=app;
}

public boolean isFrozen=false;
public String handleControllerMessage(JSONObject request) throws ValidationException {
       System.out.println("Mum Look I am controlling the app remotely !!!!"+request);
       
       String methodName=request.getString("commandName");
       Method methodToBeCalled=null;
       String message="";
       
    try {
        Method[] methods = Controller.class.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                methodToBeCalled=method;
            }

        }
        if (methodToBeCalled!=null){
            System.out.println("got message successfully"+request.get("body"));
           
            JSONObject files=request.has("files")?(JSONObject)request.get("files"):new JSONObject();
            JSONObject combinedObject =new JSONObject(files.toString());
            combinedObject.put("body",request.get("body"));
           
            

                System.out.println(combinedObject+"combined object");
                message+=methodToBeCalled.invoke(this,combinedObject);



        }else{
            return  Responder.makeErrorResponse("command not found",404);
        }
        return Responder.makeMsgResponse(message);
    } catch (SecurityException e) {
        e.printStackTrace();
        throw new ValidationException(e.getMessage());
    } catch (InvocationTargetException e) {
        throw new ValidationException(e.getMessage() );
    } catch (IllegalAccessException e) {
        throw new ValidationException(e.getMessage() );
    }


}
public void start(){
    ThreadPoolManager threadPool=this.app.getThreadPool();
    threadPool.initThreadPool(Integer.parseInt(this.app.properties.getProperty(AppsConstants.DEFAULT_MAX_Threads_PROPERTY_NAME)));
    try {
        this.app.getRabbitMQCommunicatorApp().startListening();
    }
    catch (Exception e){
        System.out.println(e.getStackTrace());
        System.exit(1);
    }
}
    public String addCommand(JSONObject request) {
        JSONObject body=(JSONObject)request.get("body");
        String commandName=(String)body.get("commandName");
        String className=(String) body.get("className");
        String commandPath=(String)body.get("commandPath") ;
        JSONObject file=(JSONObject) request.get("file");
        String fileData=file.getString("data");
        byte[] fileAsBytes=fileData.getBytes(StandardCharsets.UTF_8);




     
          
           JSONArray arr=(JSONArray) file.get("byteData");
           System.out.println(arr);
          
            byte[] b= new byte[arr.length()];
            for (int i=0;i<arr.length();i++){
                b[i]= (byte)arr.getInt(i);
               
            }
            
        this.app.classManager.addCommand(className, commandPath, b);
           if ( body.has("validationAttributes")) {
               String validationAttributes=(String) body.get("validationAttributes");
               String[] attributes = validationAttributes.split(",");
               this.app.classManager.addValidationAttributes(commandName, attributes);

           }
        return Responder.makeMsgResponse(String.format("command: %s in path: %s was set successfully",commandName,commandPath));
    }

    public String updateCommand(JSONObject request) {
       JSONObject body=(JSONObject)request.get("body");
        String commandName=(String) body.get("commandName");
    //this.app.classManager.validationMap.remove()
        if ( body.has("validationAttributes")) {
           

            this.app.classManager.validationMap.remove(commandName);

        }
       return this.addCommand(request);

    }

    public String deleteCommand(JSONObject request) {
    System.out.println("in delete command");
        JSONObject body=(JSONObject)request.get("body");
        String className=body.getString("className");
        String commandName=body.getString("commandName");
        System.out.println(className+" "+commandName);
        this.app.classManager.deleteCommand(className,commandName);
        return  Responder.makeMsgResponse(String.format("command: %s  was deleted successfully",commandName));
    }
    public String updateClassVersion(JSONObject request){
        JSONObject body=(JSONObject) request.get("body");
        String className=(String)body.getString("className");
        double classVersion=body.getDouble("classVersion");
        final Class<?> commandClass;
        try {
            commandClass = this.app.classManager.getCommand(className);
            final Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();
            return (String) commandClass.getMethod("setClassVersion", String.class,double.class).invoke(commandInstance,className,classVersion );
        } catch (ClassNotFoundException e) {
              return  Responder.makeErrorResponse("Something went wrong check that the className is correct",400);
            //throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        } catch (InstantiationException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        } catch (IllegalAccessException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        } catch (NoSuchMethodException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        }
        // creating an instance of the command class


    }
    public String getLoggingLevel(JSONObject request){
         return Responder.makeMsgResponse("logging level is: "+this.app.loggingLevel);
    }
    public String setLoggingLevel(JSONObject request){
    JSONObject body=(JSONObject) request.get("body");
    String loggingLevel=body.getString("loggingLevel");
    this.app.loggingLevel=loggingLevel;

        return Responder.makeMsgResponse("logging level is set to: "+this.app.loggingLevel +" successfully");
    }
    public String getClassVersion(JSONObject request){
        JSONObject body=(JSONObject) request.get("body");
        String className=(String)body.getString("className");
       
        final Class<?> commandClass;
        try {
            commandClass = this.app.classManager.getCommand(className);
            final Command commandInstance = (Command) commandClass.getDeclaredConstructor().newInstance();
            return (String) commandClass.getMethod("getClassVersion",String.class).invoke(commandInstance,className );
        } catch (ClassNotFoundException e) {
            return  Responder.makeErrorResponse("Something went wrong check that the className is correct",400);
            //throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        } catch (InstantiationException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        } catch (IllegalAccessException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        } catch (NoSuchMethodException e) {
            return Responder.makeErrorResponse("Something went wrong in instantiating the given class",400);
        }
      
    }

    public String freeze(Object body) {
           System.out.println("freezing the app");
        if (isFrozen) {
            return Responder.makeErrorResponse(this.app.getClass().getName()+" is already frozen",400);
        }
        try {
            this.app.getRabbitMQCommunicatorApp().pauseListening();
        } catch (IOException e) {
            e.printStackTrace();
            return Responder.makeErrorResponse("an error occurred while freezing "+this.app.getClass().getName(),400);

        }
        destroyPools();
        
        isFrozen = true;
        return Responder.makeMsgResponse(this.app.getClass().getName()+" was frozen successfully");
    }

    
    public String resume(Object body) {
        if (!isFrozen) {
              return Responder.makeErrorResponse(this.app.getClass().getName()+" is already running",400);
        }
        System.out.println("About to resume...");
        this.app.getThreadPool().initThreadPool(Integer.parseInt(this.app.properties.getProperty(AppsConstants.DEFAULT_MAX_Threads_PROPERTY_NAME)));
        System.out.println("Thread pool is ready.");
        try {
            this.app.dbInit();
        } catch (IOException e) {
            return Responder.makeErrorResponse("an error occured while re-intializing the "+this.app.getClass().getName()+"db pools",400);
           

        }

        System.out.println("DB pool is ready.");
        try {
            this.app.getRabbitMQCommunicatorApp().startListening();
        } catch (IOException e) {
            e.printStackTrace();
            destroyPools();
            return Responder.makeErrorResponse("an error occured while re-intializing the "+this.app.getClass().getName()+"rabbitmq",400);
            //System.exit(1);
        }
        System.out.println("Accepting requests from queue.");
        isFrozen = false;
        return Responder.makeMsgResponse(this.app.getClass().getName()+" was resumed successfully");
    }
    public String setMaxThreadsCount(Object body) throws IOException {
    JSONObject request=(JSONObject) body;
    request=(JSONObject) request.get("body");

    int maxThreadsCount=request.getInt("maxNumber");
        System.out.println("setting threads count to: "+maxThreadsCount);
        this.app.properties.setProperty(AppsConstants.DEFAULT_MAX_Threads_PROPERTY_NAME,""+maxThreadsCount);
        try {
            reloadThreadPool();
        } catch (IOException e) {
            e.printStackTrace();
            destroyResourcesAndExit();
            return Responder.makeErrorResponse("an error occured while re-intializing the "+this.app.getClass().getName()+"db pools",400);

        }
        System.out.println("successfully set the threads count");
        return Responder.makeMsgResponse(this.app.getClass().getName()+" max threads count was set successfully with a new value of: "+maxThreadsCount);
    }
    public String setMaxDbConnectionsCount(Object body) throws IOException {
        JSONObject request=(JSONObject) body;
        request=(JSONObject) request.get("body");
        int maxDbConnectionsCount=request.getInt("maxNumber");
        System.out.println("setting max db connections count"+maxDbConnectionsCount);
        if(this.app.properties.contains("arangodb")) {
            this.app.properties.put(AppsConstants.DEFAULT_NUMBER_OF_ARANGO_DB_PROPERTY_Name,""+maxDbConnectionsCount);
        }
        if(this.app.properties.contains("postgres")){
            this.app.properties.put(AppsConstants.DEFAULT_NUMBER_OF_POSTGRES_DB_PROPERTY_Name,""+maxDbConnectionsCount);
        }

        try {
            reloadDBPools();
        } catch (IOException e) {
            e.printStackTrace();
            destroyResourcesAndExit();
            return Responder.makeErrorResponse("an error occured while re-intializing the "+this.app.getClass().getName()+"db pools",400);
        }
        System.out.println("successfully set max db connections count");
        return Responder.makeMsgResponse(this.app.getClass().getName()+" max db connections was set successfully with a new value of: "+maxDbConnectionsCount);
    }

    private void destroyPools(){
    System.out.println("destroying pools");
        this.app.getThreadPool().releaseThreadPool();
        this.destroyDBPools();
        System.out.println("successfully destroyed pools");
        
     
    }
    private void destroyDBPools(){
    System.out.println("Destroying DB pools");
        if(this.app.properties.contains("arangodb")) {
            try {
                Arango.getInstance().destroyPool();
            } catch (PoolDoesNotExistException e) {
                throw new RuntimeException(e);
            }


        }
        if(this.app.properties.contains("postgres")){
            try {
                this.app.sqlDb.shutdownDriver();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("successfully destroyed db pools");
    }
    private void destroyResourcesAndExit() throws IOException {
        this.app.getRabbitMQCommunicatorApp().pauseListening();
        destroyPools();
       
       // System.exit(1);
    }
 
    private void reloadThreadPool() throws IOException {
        System.out.println("Reloading threads pools");
    this.app.getRabbitMQCommunicatorApp().pauseListening();

        this.app.getThreadPool().releaseThreadPool();
        this.app.getThreadPool().initThreadPool(Integer.parseInt(this.app.properties.getProperty(AppsConstants.DEFAULT_MAX_Threads_PROPERTY_NAME)));
        this.app.getRabbitMQCommunicatorApp().startListening();
    }

    private void reloadDBPools() throws IOException {
    System.out.println("Reloading Db pools");
    this.app.getRabbitMQCommunicatorApp().pauseListening();
    this.destroyDBPools();
    this.app.dbInit();
    this.app.getRabbitMQCommunicatorApp().startListening();
    }



}
