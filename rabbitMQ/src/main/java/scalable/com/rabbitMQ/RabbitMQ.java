package scalable.com.rabbitMQ;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
public class RabbitMQ {
    public Connection createConnection(String host) throws IOException, TimeoutException {
        ConnectionFactory factory=new ConnectionFactory();
        host=host==null?"localhost":host;
        factory.setHost(host);
        return  factory.newConnection();
    }
}
