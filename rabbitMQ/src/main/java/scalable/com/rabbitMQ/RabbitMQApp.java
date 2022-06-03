package scalable.com.rabbitMQ;

import com.rabbitmq.client.Connection;
import scalable.com.Interfaces.Hook;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQApp extends RabbitMQ implements  AutoCloseable{
    private Connection connection;

    public RabbitMQApp(String host) throws IOException, TimeoutException {
        this.connection=this.createConnection(host);
    }
    public RabbitMQCommunicatorApp getNewCommunicator(String queueName, Hook appHook) throws IOException {
        return  new RabbitMQCommunicatorApp(queueName,this.connection.createChannel(),appHook);
    }

    public void close() throws IOException {
        connection.close();
    }
}
