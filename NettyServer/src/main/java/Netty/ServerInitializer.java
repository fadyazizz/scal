package Netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;


public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private static final int MAX_CONTENT_LENGTH = 10 * (1 << 20);    // 10MB
    
    public ServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
                .allowedRequestHeaders("X-Requested-With", "Content-Type", "Content-Length", "Authorization", "Function-Name")
                .allowedRequestMethods(
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.DELETE,
                        HttpMethod.OPTIONS)
                .build();
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
     
        //

        p.addLast(new HttpServerCodec());
        //p.addLast(new HttpRequestEncoder());
        p.addLast(new HttpServerExpectContinueHandler());
      
        p.addLast(new CorsHandler(corsConfig));
       // p.addLast(new HttpClientCodec());
       // ;
       //
          //  p.addLast(new ObjectEncoder());
        // p.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null)));
      

        p.addLast(new HttpObjectAggregator(10*(1<<20)));
       
       p.addLast(new RequestHandler());
        p.addLast(Server.queueExecutorGroup, "QueueHandler", new QueueHandler());
        p.addLast(new ResponseHandler());
        

    }
}