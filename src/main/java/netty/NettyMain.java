package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import redis.clients.jedis.Jedis;

public class NettyMain {

    public static void main(String[] args) throws InterruptedException {
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(3);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(3);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ProxyInitializer())
                .option(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT);
        Channel serverChannel = b.bind(port).sync().channel();
        final Jedis jedis = new Jedis("localhost", 6379);
        jedis.hset("serverlist", serverName, "locahost:" + port);
        serverChannel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
            System.out.println("Server: " + serverName + " has stopped");
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> jedis.hdel("serverlist", serverName)));
    }

    public static class ProxyInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {

//            ch.pipeline().addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
//            ch.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
//            ch.pipeline().addLast(new EchoHandler());

            ch.pipeline().addLast(new HttpRequestDecoder());
            ch.pipeline().addLast(new HttpResponseEncoder());
            ch.pipeline().addLast(new CustomHttpHandler());

        }
    }
}
