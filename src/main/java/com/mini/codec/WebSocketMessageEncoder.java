package com.mini.codec;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.codec.proto.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author: zhl
 * @description: 发送给前端的websocket客户端
 **/
public class WebSocketMessageEncoder extends MessageToMessageEncoder<Message> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> out) throws JsonProcessingException {
        String jsonMessage = objectMapper.writeValueAsString(message);
        ByteBuf byteBuf = Unpooled.copiedBuffer(jsonMessage.getBytes(StandardCharsets.UTF_8));
        out.add(new TextWebSocketFrame(byteBuf));

    }
}
