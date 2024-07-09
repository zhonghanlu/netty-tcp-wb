package com.mini.codec;

import com.mini.codec.proto.Message;
import com.mini.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public class WebSocketMessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {

        ByteBuf content = msg.content();
        if (content.readableBytes() < 20) {
            return;
        }
        Message message = ByteBufToMessageUtils.transition(content);
        out.add(message);
    }
}
