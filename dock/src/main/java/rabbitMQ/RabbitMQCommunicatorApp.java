package rabbitMQ;

import com.Interfaces.Hook;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;


import java.io.IOException;
import java.nio.charset.StandardCharsets;



public class RabbitMQCommunicatorApp extends RabbitMQCommunicator {
    private String queueName;
    private String consumerTag;
    private final Hook appHook;
      public RabbitMQCommunicatorApp(String queueName, Channel channel, Hook appHook) throws IOException {
          super(channel);
          this.queueName=queueName;
          this.declareSpecificQueue(queueName);
          this.appHook= appHook;
          
      }
    public void startListening() throws IOException {
        // listening to the request queue, ready to consume a request and process it
        // using the above callback
        
        consumerTag = channel.basicConsume(queueName, false, this::onDelivery, (consumerTag -> {
        }));
    }

    public void pauseListening() throws IOException {
        channel.basicCancel(consumerTag);
        // TODO, This is just a temporary solution
        // Waiting for some 50 milliseconds because basicCancel takes some time and it leads to problems when freezing
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDelivery(String consumerTag, Delivery delivery) throws IOException {
        // creating a callback which would invoke the required command specified by the
        // JSON request message
        // adding the corrID of the response, which is the corrID of the request
        System.out.println("hereeee");
             appHook.send(consumerTag,delivery);
        
    }
    public void sendResponse(Delivery delivery,String response) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                .correlationId(delivery.getProperties().getCorrelationId()).build();
        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
                response.getBytes(StandardCharsets.UTF_8));

        // sending a manual acknowledgement of sending the response
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

}
