package scalable.com.shared.classes;

import Netty.ServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


import java.net.InetSocketAddress;

public class BackdoorServer {
    private final int port;
    private final Controller controller;

    public BackdoorServer(int port, Controller controller) {
        this.port = port;
        this.controller= controller;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1); //Creates an EventLoop that is shareable across clients
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group) // Bootstrap the server to a specific group
                    .channel(NioServerSocketChannel.class) // Specifies transport protocol for channel
                    .localAddress(new InetSocketAddress(port)) // Specifies address for channel
                    .childHandler(new BackDoorServerInitializer(controller));

            ChannelFuture f = b.bind().sync(); // Bind server to address, and block (sync method) until it does so

            System.out.println(BackdoorServer.class.getSimpleName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync(); // Returns a future channel that will be notified when shutdown

        } finally {
            group.shutdownGracefully().sync(); // Terminates all threads
        }
    }
}
