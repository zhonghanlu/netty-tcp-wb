package com.mini.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WebSocketServerConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean(destroyMethod = "stop")
    public WebSocketServer webSocketServer(ExecutorService executor) {
        WebSocketServer server = new WebSocketServer();
        executor.submit(server);
        return server;
    }
    public static class WebSocketServer implements Runnable {

        private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        private final EventLoopGroup workerGroup = new NioEventLoopGroup();

        private ChannelFuture future;

        public void stop() {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

        @Override
        public void run() {
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new WebSocketServerInitializer());

                future = b.bind(8080).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                stop();
            }
        }
    }
}