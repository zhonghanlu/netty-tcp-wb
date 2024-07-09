package com.mini.codec;

import com.mini.codec.proto.Message;
import com.mini.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @description: 消息解码类
 * @author: lld
 * @version: 1.0
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) {

        if (in.readableBytes() < 20) {
            return;
        }

        Message message = ByteBufToMessageUtils.transition(in);

        out.add(message);
    }
}
