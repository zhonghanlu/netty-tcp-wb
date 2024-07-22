package com.mini.netty.websocket.msgpack;

import com.mini.codec.proto.MessagePack;

import java.lang.reflect.Field;

/**
 * @author zhl
 * @create 2024/7/22 9:12
 */
public class MsgPack {

    /**
     * 顺序拼接
     */
    public static String handlerPack(MessagePack messagePack) {
        StringBuilder sb = new StringBuilder();
        // 反射获取所有字段
        for (Field field : messagePack.getClass().getDeclaredFields()) {
            // 设置为可访问，以便访问私有字段
            field.setAccessible(true);
            try {
                // 拼接字段值
                sb.append(field.get(messagePack));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return sb.toString().trim(); // 去掉最后一个空格
    }

}
