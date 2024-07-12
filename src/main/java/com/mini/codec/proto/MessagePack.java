package com.mini.codec.proto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mini.codec.ByteArrayDeserializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: zhl
 * @description: 消息服务发送给tcp的包体, tcp再根据改包体解析成Message发给客户端
 **/
@Data
public class MessagePack<T> implements Serializable {

    /**
     * 包头
     * OxAA
     * 1 字节
     */
    private byte command;

    /**
     * 设备编号
     * 1 字节
     */
    private byte deviceNo;

    /**
     * 运行指令
     * 1 字节
     */
    private byte runCommand;

    /**
     * 刺激类型
     * 1 字节
     */
    private byte exciteType;

    /**
     * 输出电流
     * 2 字节
     */
    private short electricityOut;

    /**
     * 保留字段
     * 13 字节
     */
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[] extra; // 使用字节数组来表示13字节的保留字段

    /**
     * 校验字段
     * 1 字节
     */
    private byte verify;

}
