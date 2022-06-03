package Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import scalable.com.rabbitMQ.RabbitMQServer;

import javax.net.ssl.SSLException;

import java.io.FileReader;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class Server {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
    public static final AttributeKey<HttpRequest> REQ_KEY = AttributeKey.valueOf("req");
    public static final AttributeKey<String> QUEUE_KEY = AttributeKey.valueOf("queue");
    public static  ArrayList<String> apps ;
    public static  Properties p;
    public static final int nThreads = 8;
    protected static final EventExecutorGroup queueExecutorGroup = new DefaultEventExecutorGroup(nThreads);
    public static EventLoopGroup bossGroup;
    public static EventLoopGroup workerGroup;
    protected static RabbitMQServer rabbitMQServer;
    private static void initializeRabbitMQ() throws IOException, TimeoutException {
        
        

        rabbitMQServer=new RabbitMQServer(p.getProperty("rabbitmq_host"));
    }
    private static void loadProperties() throws IOException {
        p=new Properties();
        p.load(Server.class.getClassLoader().getResourceAsStream("ServerConfig.properties"));

       
    }
    private static  void loadApps(){
        String[] temp=p.getProperty("apps").split(",");
        apps=new ArrayList<String>();
        Collections.addAll(apps,temp);
    }
    public static void main(String[] args) throws CertificateException, IOException, InterruptedException, TimeoutException {
        // Configure SSL.

            loadProperties();
           loadApps();
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        //TODO check whether increasing the number of boss threads would differ or not
        bossGroup = new NioEventLoopGroup();
        //by default 2*numberofcores
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            initializeRabbitMQ();
            //so_backlog is the number of requests that can be queued if busy
            // a possible TODO is automatically spawn a new server instance when the server starts refusing connections
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            System.out.println("Open your web browser and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            shutdownGracefully();
        }
    }

    public static void shutdownGracefully() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
