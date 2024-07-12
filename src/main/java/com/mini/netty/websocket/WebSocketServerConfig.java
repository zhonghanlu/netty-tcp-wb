package com.mini.netty.websocket;

import com.mini.IPUtils;
import com.mini.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WebSocketServerConfig {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServerConfig.class);

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Bean(destroyMethod = "stop")
    public WebSocketServer webSocketServer(ExecutorService executorService) {
        WebSocketServer server = new WebSocketServer();
        executorService.submit(server);
        return server;
    }

    @Component
    public static class WebSocketServer implements Runnable {

        private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        private final EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

        private ChannelFuture future;

        public void stop() {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

        @Override
        public void run() {
            // 根据配置是否启动websocket
            if (!NettyProperties.WEB_SOCKET_ENABLED) {
                return;
            }
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)          // 连接队列长度
                    .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)  // 保持连接活动
                    .childHandler(new WebSocketServerInitializer());

                future = b.bind(NettyProperties.WEB_SOCKET_PORT).sync();
                log.info("【websocket构建启动成功！地址：ws://{}:{}{}】", IPUtils.getIp(), NettyProperties.WEB_SOCKET_PORT, NettyProperties.WEB_SOCKET_PREFIX);
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                stop();
            }
        }
    }
}
