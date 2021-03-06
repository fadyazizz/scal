package shared.classes;



import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;


public class BackDoorServerInitializer extends ChannelInitializer<SocketChannel> {

     Controller controller;
       public BackDoorServerInitializer(Controller controller){
           this.controller=controller;
       }

    @Override
    public void initChannel(SocketChannel ch) {
        
        ChannelPipeline p = ch.pipeline();


        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpResponseEncoder());
        p.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
        
        p.addLast(new HttpObjectAggregator(100*(1<<20)));
        p.addLast(new BackdoorServerHandler(controller));
        p.addLast(new BackDoorResponseHandler());

        


    }
}