package com.SpotifyButHalfAsGood;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory=new ConnectionFactory();

        //this is by default localhost
        //factory.setHost("localhost");
        //get a connection to rabbit mq
        //wrap it around try to close the connection if an error occurs
        Connection connection=factory.newConnection();
            //the api itself is exposed through channels
            Channel channel=connection.createChannel();
            //creating a queue (even if the sender created it)
            channel.queueDeclare("agmadQueue",false,false,false,null);
            channel.basicConsume("agmadQueue", true, new DeliverCallback() {
                @Override
                public void handle(String consumerTag, Delivery message) throws IOException {
                            byte[] mess= message.getBody();
                            System.out.println("I jus received a message: "+new String(mess,"UTF-8"));
                }
            }, new CancelCallback() {
                @Override
                public void handle(String consumerTag) throws IOException {
                    
                }
            });
        
        
    }


}
