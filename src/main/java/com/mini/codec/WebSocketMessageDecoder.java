package com.mini.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.codec.proto.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

/**
 * @description: 处理前端传入json，直接转为实体
 * @author: zhl
 */
public class WebSocketMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) throws JsonProcessingException {
        String payload = frame.text(); // 直接从TextWebSocketFrame中获取文本内容
        Message message = objectMapper.readValue(payload, Message.class);
        out.add(message);
    }
}
