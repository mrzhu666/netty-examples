package io.netty.example.discard;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class DiscardClient {
    private final String host;
    private final int port;
    private Channel channel;

    public DiscardClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        String message = "message\n";
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                 }
             });

            // Start the client
            ChannelFuture f = b.connect(host, port).sync();
            this.channel = f.channel();
            System.out.println("Connected to server " + host + ":" + port);

            // Schedule sending messages every 1 second
            channel.eventLoop().scheduleAtFixedRate(() -> {
                if (channel.isActive()) {
                    channel.writeAndFlush(message);
                }
            }, 0, 1, TimeUnit.SECONDS);

            // Wait until the connection is closed
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080;
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        new DiscardClient(host, port).start();
    }
}
