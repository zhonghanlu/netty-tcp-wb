//package com.mini.netty.client;
//
//import com.mini.NettyProperties;
//import com.mini.codec.MessageDecoder;
//import com.mini.codec.MessageEncoder;
//import com.mini.netty.client.handler.HeartBeatTcpClientHandler;
//import com.mini.netty.client.handler.NettyTcpClientHandler;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.FixedLengthFrameDecoder;
//import io.netty.handler.timeout.IdleStateHandler;
//import io.netty.util.concurrent.DefaultEventExecutor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PreDestroy;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@Slf4j
//@Component
//public class OptimizedNettyTcpClient implements ApplicationRunner {
//
//    private final EventLoopGroup group = new NioEventLoopGroup();
//
//    private final Bootstrap bootstrap = new Bootstrap();
//
//    private Channel channel;
//
//    /**
//     * 断线重连并且发布一个监听事件
//     */
//    private final AtomicBoolean connecting = new AtomicBoolean(false);
//
//    private final DefaultEventExecutor executor = new DefaultEventExecutor(group);
//
//    /**
//     * 标志位，用于记录监听器是否已经添加
//     */
//    private boolean closeFutureListenerAdded = false;
//
//    public OptimizedNettyTcpClient() {
//        log.info("OptimizedNettyTcpClient instance created.");
//        bootstrap.group(group)
//            .channel(NioSocketChannel.class)
//            .handler(new ChannelInitializer<NioSocketChannel>() {
//                @Override
//                protected void initChannel(NioSocketChannel ch) {
//                    ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
//                    ch.pipeline().addLast(new MessageDecoder());
//                    ch.pipeline().addLast(new MessageEncoder());
//                    //readerIdleTime：超过xxx时间客户端没有发生读事件，就会触发一个 READER_IDLE 的 IdleStateEvent 事件.
//                    //writerIdleTime：超过xxx时间客户端没有发生写事件，就会触发一个 WRITER_IDLE 的 IdleStateEvent 事件.
//                    //allIdleTime：超过xxx时间客户端没有发生读或写事件，就会触发一个 ALL_IDLE 的 IdleStateEvent 事件.
//                    //unit：时间参数的格式
//                    ch.pipeline().addLast(new IdleStateHandler(40, 0, 0, TimeUnit.SECONDS));
//                    ch.pipeline().addLast(new HeartBeatTcpClientHandler());
//                    ch.pipeline().addLast(new NettyTcpClientHandler());
//                }
//            });
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        if (NettyProperties.TCP_SERVER_ENABLED) {
//            doConnect();
//        }
//    }
//
//    /**
//     * 重试监听
//     */
//    private void addCloseFutureListenerIfNecessary() {
//        if (channel != null && channel.isActive()) {
//            if (!closeFutureListenerAdded) {
//                channel.closeFuture().addListener((ChannelFutureListener) f -> {
//                    log.error("Connection lost. Attempting to reconnect. Retrying in {} seconds.",
//                        NettyProperties.TCP_SERVER_RETRYING_INTERVAL);
//                    executor.schedule(this::doConnect, NettyProperties.TCP_SERVER_RETRYING_INTERVAL, TimeUnit.SECONDS);
//                });
//                // 标志位设置为true，表示已经添加过监听器
//                closeFutureListenerAdded = true;
//            } else {
//                // 移除旧的监听器，然后重新添加，确保监听器总是针对当前的channel
//                channel.closeFuture().removeListener((ChannelFutureListener) f -> {
//                });
//                channel.closeFuture().addListener((ChannelFutureListener) f -> {
//                    log.error("Connection lost. Attempting to reconnect. Retrying in {} seconds.",
//                        NettyProperties.TCP_SERVER_RETRYING_INTERVAL);
//                    executor.schedule(this::doConnect, NettyProperties.TCP_SERVER_RETRYING_INTERVAL, TimeUnit.SECONDS);
//                });
//            }
//        }
//    }
//
//    /**
//     * 建立连接
//     */
//    public void doConnect() {
//        if (channel == null || !channel.isActive() && (connecting.compareAndSet(false, true))) {
//            try {
//                ChannelFuture future = bootstrap.connect(NettyProperties.TCP_SERVER_HOST, NettyProperties.TCP_SERVER_PORT);
//                future.addListener((ChannelFutureListener) f -> {
//                    if (f.isSuccess()) {
//                        channel = f.channel();
//                        log.info("Connected to TCP server at {}:{}", NettyProperties.TCP_SERVER_HOST,
//                            NettyProperties.TCP_SERVER_PORT);
//                        // 添加监听处理后续断线重连
//                        if (NettyProperties.TCP_SERVER_RETRYING_ENABLED) {
//                            addCloseFutureListenerIfNecessary();
//                        }
//                    } else {
//                        if (NettyProperties.TCP_SERVER_RETRYING_ENABLED) {
//                            log.error("Failed to connect to server at {}:{}. Retrying in {} seconds.",
//                                NettyProperties.TCP_SERVER_HOST, NettyProperties.TCP_SERVER_PORT,
//                                NettyProperties.TCP_SERVER_RETRYING_INTERVAL);
//                            executor.schedule(this::doConnect, NettyProperties.TCP_SERVER_RETRYING_INTERVAL, TimeUnit.SECONDS);
//                        }
//                    }
//                });
//            } finally {
//                connecting.set(false);
//            }
//        }
//    }
//
//
//    @PreDestroy
//    public void shutdown() {
//        log.info("Shutting down OptimizedNettyTcpClient...");
//        connecting.set(false);
//        group.shutdownGracefully().syncUninterruptibly();
//        executor.shutdownGracefully().syncUninterruptibly();
//    }
//}
