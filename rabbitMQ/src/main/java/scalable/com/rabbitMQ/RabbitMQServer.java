package scalable.com.rabbitMQ;

import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQServer extends RabbitMQ implements AutoCloseable {
      private Connection connection;
      public RabbitMQServer(String host) throws IOException, TimeoutException {
          this.connection=this.createConnection(host);
      }
      
      public RabbitMQCommunicatorServer getNewCommunicator() throws IOException {
          return  new RabbitMQCommunicatorServer(this.connection.createChannel());
      }

    public void close() throws IOException {
        connection.close();
    }
}
