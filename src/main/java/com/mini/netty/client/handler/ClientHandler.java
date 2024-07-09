package com.mini.netty.client.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.netty.websocket.handler.WebSocketServerHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

public class ClientHandler extends io.netty.channel.ChannelInboundHandlerAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 当通道激活时，可以发送初始化消息
        ctx.writeAndFlush("Hello from client!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws JsonProcessingException {
        String data = (String) msg;
//        System.out.println("Received from server: " + data);
        // 将数据转发给 WebSocket 服务器
        Map<String, Integer> map = objectMapper.readValue(data, Map.class);
        map.forEach((key, value) -> {
            WebSocketServerHandler.sendToWebSocket(key, String.valueOf(value));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}