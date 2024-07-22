package com.mini.netty.server;

import com.mini.NettyProperties;
import com.mini.codec.MessageDecoder;
import com.mini.netty.server.handler.NettyTcpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OptimizedNettyTcpServer implements ApplicationRunner {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final ServerBootstrap bootstrap = new ServerBootstrap();

    private Channel channel;

    public OptimizedNettyTcpServer() {
        log.info("OptimizedNettyTcpServer instance created.");
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new StringEncoder()); // 发暂且不转码
//                    ch.pipeline().addLast(new MessageEncoder());
                        ch.pipeline().addLast(new IdleStateHandler(40, 0, 0, TimeUnit.SECONDS));
//                    ch.pipeline().addLast(new HeartBeatTcpServerHandler());
                        ch.pipeline().addLast(new NettyTcpServerHandler());
                    }
                });
    }

    @Override
    public void run(ApplicationArguments args) {
        if (NettyProperties.TCP_SERVER_ENABLED) {
            bind();
        }
    }

    /**
     * 绑定服务端口
     */
    private void bind() {
        ChannelFuture future = bootstrap.bind(NettyProperties.TCP_SERVER_HOST, NettyProperties.TCP_SERVER_PORT);
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                channel = f.channel();
                log.info("TCP server is listening on {}:{}", NettyProperties.TCP_SERVER_HOST,
                        NettyProperties.TCP_SERVER_PORT);
            } else {
                log.error("Failed to bind server at {}:{}. Shutting down.", NettyProperties.TCP_SERVER_HOST,
                        NettyProperties.TCP_SERVER_PORT);
                shutdown();
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down OptimizedNettyTcpServer...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
