package com.SpotifyButHalfAsGood;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Sender {

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory=new ConnectionFactory();

        //this is by default localhost
        //factory.setHost("localhost");
        //get a connection to rabbit mq
        //wrap it around try to close the connection if an error occurs
        try(Connection connection=factory.newConnection()){
            //the api itself is exposed through channels
            Channel channel=connection.createChannel();
            //creating a queue
            channel.queueDeclare("agmadQueue",false,false,false,null);
            String message="ana gamed awii !!!!";
            channel.basicPublish("","agmadQueue",false,null,message.getBytes());
            System.out.println("message has been sent!!!");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


}
