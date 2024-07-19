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
        messageHeader.setDeviceNo(in.readByte());
        messageHeader.setFlag(in.readByte());
        messageHeader.setHospNoH(in.readByte());
        messageHeader.setHospNoL(in.readByte());
        messageHeader.setDeviceNoExtra(in.readByte());
        messageHeader.setRunningState(in.readByte());
        messageHeader.setExciteType(in.readByte());
        messageHeader.setElectricityH(in.readByte());
        messageHeader.setElectricityL(in.readByte());
        messageHeader.setResistanceH(in.readByte());
        messageHeader.setResistanceL(in.readByte());
        messageHeader.setBattery(in.readByte());
        messageHeader.setSignal(in.readByte());
        messageHeader.setResidueTimeH(in.readByte());
        messageHeader.setResidueTimeL(in.readByte());
        messageHeader.setExtraA(in.readByte());
        messageHeader.setExtraB(in.readByte());
        messageHeader.setExtraC(in.readByte());
        messageHeader.setExtraD(in.readByte());
        messageHeader.setVerify(in.readByte());

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        in.markReaderIndex();
        return message;
    }

}
