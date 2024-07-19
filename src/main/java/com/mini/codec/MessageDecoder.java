package com.mini.codec;

import com.mini.codec.proto.Message;
import com.mini.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @description: 接收TCP服务端数据
 * @author: zhl
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        if (in.readableBytes() != 20) {
            log.error("接收字节数据载荷大小有误");
            return;
        }

        // TODO: 校验

        Message message = ByteBufToMessageUtils.transition(in);

        out.add(message);
    }
}
