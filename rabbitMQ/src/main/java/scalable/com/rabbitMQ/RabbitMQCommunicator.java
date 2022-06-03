package scalable.com.rabbitMQ;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class RabbitMQCommunicator implements  AutoCloseable {
    protected final Channel channel;
    
    public RabbitMQCommunicator(Channel channel) {
        this.channel = channel;
    }

    protected String declareSpecificQueue(String queue) throws IOException {
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        return channel.queueDeclare(queue, durable, exclusive, autoDelete, null).getQueue();
    }

    @Override
    public void close() throws IOException, TimeoutException {
        channel.close();
    }
}
