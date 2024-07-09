package com.mini.codec.utils;

import com.mini.codec.proto.Message;
import com.mini.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;

/**
 * @author: zhl
 * @description: 将ByteBuf转化为Message实体，根据私有协议转换
 */
public class ByteBufToMessageUtils {

    public static Message transition(ByteBuf in) {

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCommand(in.readByte() & 0xFF);
        messageHeader.setHospNo(in.readShort());
        messageHeader.setDeviceNo(in.readByte());
        messageHeader.setRunningState(in.readByte());
        messageHeader.setExciteType(in.readByte());
        messageHeader.setElectricity(in.readShort());
        messageHeader.setResistance(in.readShort());
        messageHeader.setBattery(in.readShort());
        messageHeader.setSignal(in.readShort());
        messageHeader.setResidueTime(in.readByte());
        messageHeader.setExtra(in.readInt());
        messageHeader.setVerify(in.readByte());

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        in.markReaderIndex();
        return message;
    }

}
