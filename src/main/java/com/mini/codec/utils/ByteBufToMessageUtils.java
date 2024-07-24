package com.mini.codec.utils;

import com.mini.codec.enums.ExciteType;
import com.mini.codec.enums.RunOrStop;
import com.mini.codec.proto.Message;
import com.mini.codec.proto.MessageHeader;
import com.mini.codec.proto.MessageVo;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author: zhl
 * @description: 将ByteBuf转化为Message实体，根据私有协议转换
 */
@Slf4j
public class ByteBufToMessageUtils {

    private static Map<Integer, String> signalLevelMap = new TreeMap<>(Comparator.reverseOrder());

    static {
        signalLevelMap.put(-100, "1");
        signalLevelMap.put(-88, "2");
        signalLevelMap.put(-77, "3");
        signalLevelMap.put(-55, "4");
        signalLevelMap.put(Integer.MIN_VALUE, "0");
    }


    public static Message transition(ByteBuf in) {

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setDeviceNo(in.readUnsignedByte());
        messageHeader.setFlag(in.readByte());
        messageHeader.setHospNoH(in.readByte());
        messageHeader.setHospNoL(in.readByte());
        messageHeader.setDeviceNoExtra(in.readUnsignedByte());
        messageHeader.setRunningState(in.readByte());
        messageHeader.setExciteType(in.readByte());
        messageHeader.setElectricityH(in.readUnsignedByte());
        messageHeader.setElectricityL(in.readUnsignedByte());
        messageHeader.setResistanceH(in.readUnsignedByte());
        messageHeader.setResistanceL(in.readUnsignedByte());
        messageHeader.setBattery(in.readUnsignedByte());
        messageHeader.setSignal(in.readUnsignedByte());
        messageHeader.setResidueTimeH(in.readUnsignedByte());
        messageHeader.setResidueTimeL(in.readUnsignedByte());
        messageHeader.setExtraA(in.readByte());
        messageHeader.setExtraB(in.readByte());
        messageHeader.setExtraC(in.readByte());
        messageHeader.setExtraD(in.readByte());
        messageHeader.setVerify(in.readByte());


        // 处理返回Vo
        MessageVo messageVo = new MessageVo();
        // 设备编号
        messageVo.setDeviceNo(String.valueOf(messageHeader.getDeviceNo()));
        // 医院编号
        messageVo.setHospNo(String.valueOf(messageHeader.getHospNoH() * 256 + messageHeader.getHospNoL()));
        // 运行状态
        messageVo.setRunningState(RunOrStop.fromStringValue(String.valueOf(messageHeader.getRunningState())));
        // 刺激类型
        messageVo.setExciteType(ExciteType.fromStringValue(String.valueOf(messageHeader.getExciteType())));
        // 电流
        String electricity = String.valueOf(messageHeader.getElectricityH() * 256 + messageHeader.getElectricityL());
        messageVo.setElectricity(electricity);
        // 阻抗
        String resistance = String.valueOf(messageHeader.getResistanceH() * 256 + messageHeader.getResistanceL());
        messageVo.setResistance(resistance);
        // 电池电量
        messageVo.setBattery(String.valueOf(messageHeader.getBattery()));
        // 信号强度
        messageVo.setSignal(evaluateSignal(messageHeader.getSignal()));
        // 剩余时间
        long time = messageHeader.getResidueTimeH() * 256 + messageHeader.getResidueTimeL();
        BigDecimal bg = new BigDecimal(time);
        BigDecimal toBg = new BigDecimal(60);
        BigDecimal divide = bg.divide(toBg, 2, RoundingMode.HALF_UP);
        messageVo.setResidueTime(divide.toString());
        // 拓展A
        messageVo.setExtraA(String.valueOf(messageHeader.getExtraA()));
        // 拓展B
        messageVo.setExtraB(String.valueOf(messageHeader.getExtraB()));
        // 拓展C
        messageVo.setExtraC(String.valueOf(messageHeader.getExtraC()));
        // 拓展D
        messageVo.setExtraD(String.valueOf(messageHeader.getExtraD()));
        // 校验字段
        messageVo.setVerify(String.valueOf(messageHeader.getVerify()));

        Message message = new Message();
        message.setMessageHeader(messageHeader);
        message.setMessageVo(messageVo);

        in.markReaderIndex();
        return message;
    }

    public static String evaluateSignal(long signal) {
        if (signal >= -100 && signal < -88) {
            return "1";
        } else if (signal >= -88 && signal < -77) {
            return "2";
        } else if (signal >= -66 && signal < -55) {
            return "3";
        } else if (signal >= -55) {
            return "4";
        } else {
            log.info("Signal is below -100");
            return "0";
        }
    }

}
