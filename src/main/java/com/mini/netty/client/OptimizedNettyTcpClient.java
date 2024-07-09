package com.mini.netty.client;

import com.mini.codec.MessageDecoder;
import com.mini.netty.client.handler.HeartBeatTcpClientHandler;
import com.mini.netty.client.handler.NettyTcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mini.common.constant.NettyServerConstant.*;

@Slf4j
@Component
public class OptimizedNettyTcpClient implements ApplicationRunner {

    @Value("${tcp.client.host}")
    private String host;

    @Value("${tcp.client.port}")
    private int port;

    private final EventLoopGroup group = new NioEventLoopGroup();

    private final Bootstrap bootstrap = new Bootstrap();

    private Channel channel;

    public OptimizedNettyTcpClient() {
        log.info("OptimizedNettyTcpClient instance created.");
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
//                        ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
//                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        //readerIdleTime：超过xxx时间客户端没有发生读事件，就会触发一个 READER_IDLE 的 IdleStateEvent 事件.
                        //writerIdleTime：超过xxx时间客户端没有发生写事件，就会触发一个 WRITER_IDLE 的 IdleStateEvent 事件.
                        //allIdleTime：超过xxx时间客户端没有发生读或写事件，就会触发一个 ALL_IDLE 的 IdleStateEvent 事件.
                        //unit：时间参数的格式
                        ch.pipeline().addLast(new IdleStateHandler(40, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new HeartBeatTcpClientHandler());
                        ch.pipeline().addLast(new NettyTcpClientHandler());
                    }
                });
    }

    @Override
    public void run(ApplicationArguments args) {
        doConnect();
    }

    /**
     * 断线重连并且发布一个监听事件
     */
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    private final DefaultEventExecutor executor = new DefaultEventExecutor(group);

    private boolean closeFutureListenerAdded = false; // 标志位，用于记录监听器是否已经添加

    private void addCloseFutureListenerIfNecessary() {
        if (channel != null && channel.isActive() && !closeFutureListenerAdded) {
            channel.closeFuture().addListener((ChannelFutureListener) f -> {
                log.error("Connection lost. Attempting to reconnect.");
                executor.schedule(this::doConnect, 3, TimeUnit.SECONDS);
            });
            closeFutureListenerAdded = true;
        }
    }

    public void doConnect() {
        if (channel == null || !channel.isActive() && (connecting.compareAndSet(false, true))) {
            try {
                ChannelFuture future = bootstrap.connect(host, port);
                future.addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        channel = f.channel();
                        log.info("Connected to TCP server at {}:{}", host, port);
                        addCloseFutureListenerIfNecessary();
                    } else {
                        log.error("Failed to connect to server at {}:{}. Retrying in 3 seconds.", host, port);
                        executor.schedule(this::doConnect, 3, TimeUnit.SECONDS);
                    }
                });
            } finally {
                connecting.set(false);
            }
        }
    }


    @PreDestroy
    public void shutdown() {
        log.info("Shutting down OptimizedNettyTcpClient...");
        connecting.set(false);
        group.shutdownGracefully().syncUninterruptibly();
        executor.shutdownGracefully().syncUninterruptibly();
    }
}
