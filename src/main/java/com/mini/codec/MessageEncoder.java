//package com.mini.codec;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mini.codec.proto.MessagePack;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.MessageToByteEncoder;
//
///**
// * @author: Chackylee
// * @description: 消息编码类，私有协议规则，前4位表示长度，接着command4位，后面是数据
// **/
//public class MessageEncoder extends MessageToByteEncoder {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
//        if (msg instanceof MessagePack) {
//            MessagePack msgBody = (MessagePack) msg;
//            String s = objectMapper.writeValueAsString(msg);
//            byte[] bytes = s.getBytes();
//            out.writeInt(msgBody.getCommand());
//            out.writeInt(bytes.length);
//            out.writeBytes(bytes);
//        }
//    }
//
//}
