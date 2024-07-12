package com.mini.netty.websocket;

import com.mini.NettyProperties;
import com.mini.codec.WebSocketMessageDecoder;
import com.mini.codec.WebSocketMessageEncoder;
import com.mini.netty.websocket.handler.WebSocketServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

import static com.mini.constant.NettyServerConstant.WEBSOCKET_PROTOCOL;


@Component
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new WebSocketMessageDecoder());
        p.addLast(new WebSocketMessageEncoder());
        // 与WebSocketServerProtocolHandler切换位置，主要为了确保握手成功前取地址内的参数
        p.addLast(new WebSocketServerHandler());
        // checkStartsWith 必须为TRUE 才能使用地址参数
        p.addLast(new WebSocketServerProtocolHandler(NettyProperties.WEB_SOCKET_PREFIX, WEBSOCKET_PROTOCOL, true, 10485760));
    }
}
