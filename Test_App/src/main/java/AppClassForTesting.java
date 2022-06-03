

import com.rabbitmq.client.Delivery;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.quarkus.arc.impl.Reflections;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scalable.com.Interfaces.Hook;
import scalable.com.rabbitMQ.RabbitMQApp;
import scalable.com.rabbitMQ.RabbitMQCommunicatorApp;
import scalable.com.rabbitMQ.RabbitMQCommunicatorServer;
import scalable.com.rabbitMQ.RabbitMQServer;
import scalable.com.shared.AppsConstants;
import scalable.com.shared.classes.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AppClassForTesting  {

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
    public abstract JSONObject getExpectedResponse();

    public  String loggingLevel="normal";

    public void dbInit() throws IOException {

     

    }

    protected  void start() throws IOException, TimeoutException, ClassNotFoundException {



        this.initRabbitMQ();

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
          try{

              response=this.getExpectedResponse().toString();
       

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // sending response message to the response queue, which is defined by the
            // request message's properties
            rabbitMQCommunicatorApp.sendResponse(delivery,response);

        }
    }

  

    
    




}


