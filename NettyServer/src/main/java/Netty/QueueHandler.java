package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import Netty.Server;
import scalable.com.rabbitMQ.RabbitMQCommunicatorServer;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class QueueHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) {
        ByteBuf buffer = (ByteBuf) object;
        String request = buffer.toString(CharsetUtil.UTF_8);
           //TODO get the queue Name Correctly
        //TODO explore the possibility of attaching a call back instead of waiting for the response
        String queueName = ctx.channel().attr(Server.QUEUE_KEY).get();

        queueName = queueName.toUpperCase();
       
        String reqQueueName = queueName + "Server";

        
        String resQueueName = queueName + "App";
        try (RabbitMQCommunicatorServer channel = Server.rabbitMQServer.getNewCommunicator()) {
            String response = channel.placeRequestInQueue(request, reqQueueName, resQueueName);
            ByteBuf content = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8);
            ctx.fireChannelRead(content.copy());
        } catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}