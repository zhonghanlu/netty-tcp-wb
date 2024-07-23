package com.mini.codec;

import com.mini.codec.proto.Message;
import com.mini.codec.proto.MessagePack;
import com.mini.codec.utils.MsgPack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhl
 * @description: 消息编码类，私有协议规则，返回至TCP服务端，入参消息为websocket转换得来
 **/
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final int FIXED_MESSAGE_SIZE = 20; // 消息固定长度

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {

        byte[] bytes = serializeMessage(msg.getMessagePack());

//        // 处理有效载荷
//        byte[] bytes1 = padPayloadToFixedLength(bytes);
//
//        if (Objects.isNull(bytes1)) {
//            return;
//        }

        out.writeBytes(bytes);
    }

    /**
     * 将有效载荷扩展至固定长度的消息。
     *
     * @param payload 有效载荷字节数组
     * @return 固定长度的消息字节数组
     */
    public static byte[] padPayloadToFixedLength(byte[] payload) {
        if (payload.length > FIXED_MESSAGE_SIZE) {
            log.error("有效载荷大于总载荷，有效载荷：{}", payload.length);
            return null;
        }

        ByteBuf byteBuf = Unpooled.buffer(FIXED_MESSAGE_SIZE);

        // 先写入有效载荷
        byteBuf.writeBytes(payload);

        // 计算需要填充的字节数
        int paddingSize = FIXED_MESSAGE_SIZE - payload.length;

        // 填充剩余的字节
        byteBuf.writeBytes(new byte[paddingSize]);

        // 读取ByteBuf中的字节到数组
        byte[] paddedMessage = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(paddedMessage);

        // 释放ByteBuf资源
        byteBuf.release();

        return paddedMessage;
    }

    /**
     * 序列化MessagePack对象为固定长度的字节数组。
     *
     * @param msgBody MessagePack对象
     * @return 固定长度的字节数组
     */
    public static byte[] serializeMessage(MessagePack msgBody) {
        String optCommand = MsgPack.handlerPack(msgBody);
        return optCommand.getBytes();
    }


}
