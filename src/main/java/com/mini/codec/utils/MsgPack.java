package com.mini.codec.utils;


import com.mini.codec.enums.Command;
import com.mini.codec.proto.MessageDTO;
import com.mini.codec.proto.MessagePack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.mini.constant.NettyServerConstant.TCP_MESSAGE_ZERO_PADDING;

/**
 * @author zhl
 * @create 2024/7/22 9:12
 */
@Slf4j
public class MsgPack {

    /**
     * 顺序拼接
     */
    public static String handlerPack(MessagePack messagePack) {
        String optToSendCommand = "";
        String command = messagePack.getOptCommand().getCommand();

        List<String> optA = Arrays.asList(Command.HA1.getStringValue(), Command.HA0.getStringValue(),
            Command.BP0.getStringValue(), Command.BP1.getStringValue());

        List<String> optB = Arrays.asList(Command.SS.getStringValue(), Command.PS.getStringValue(),
            Command.PB.getStringValue(), Command.ZZ.getStringValue(), Command.RR.getStringValue());

        // 是否允许手动设置  蜂鸣器打开/关闭 补0返回
        if (optA.contains(command)) {
            optToSendCommand = command + TCP_MESSAGE_ZERO_PADDING;
            log.info("webSocket客户端主动调用：{}", optToSendCommand);
            return optToSendCommand;
        }

        // 启动 BB + 设备id  暂且不知道其他几种是否需要适配
        if (Command.BB.getStringValue().equals(command)) {
            optToSendCommand = command + messagePack.getOptCommand().getClientId();
            log.info("webSocket客户端主动调用：{}", optToSendCommand);
            return optToSendCommand;
        }

        // 基础类型，直接发送
        if (optB.contains(command)) {
            optToSendCommand = command;
            log.info("webSocket客户端主动调用：{}", optToSendCommand);
            return optToSendCommand;
        }

        //  处理基础类型
        MessageDTO messageDTO = new MessageDTO();
        BeanUtils.copyProperties(messagePack, messageDTO);
        messageDTO.setCommand(Command.C.getStringValue());
        // 通过反射拼接
        optToSendCommand = reflectiveSplicing(messageDTO);
        log.info("webSocket客户端主动调用：{}", optToSendCommand);
        return optToSendCommand;
    }


    /**
     * 通过反射拼接所有字段
     */
    public static String reflectiveSplicing(MessageDTO messageDTO) {
        StringBuilder sb = new StringBuilder();
        // 反射获取所有字段
        for (Field field : messageDTO.getClass().getDeclaredFields()) {
            // 设置为可访问，以便访问私有字段
            field.setAccessible(true);
            try {
                // 拼接字段值
                sb.append(field.get(messageDTO));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return sb.toString().trim(); // 去掉最后一个空格
    }


}
