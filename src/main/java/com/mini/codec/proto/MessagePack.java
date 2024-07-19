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

    ////////////////////////////////////hex请求参 无需Encoder/////////////////////////////////////////////

    /**
     * 操作hex
     */
    private String optCommand;

    /**
     * 刺激类型 (A:TDCS;B:TPCS;C:TACS;D:TRNS)
     */
    private String exciteTypePack;

    /**
     * 方向 0正 1负
     */
    private String direction;

    /**
     * 电流1
     */
    private String electricity1;

    /**
     * 电流2
     */
    private String electricity2;

    /**
     * 电流3
     */
    private String electricity3;

    /**
     * 电流4
     */
    private String electricity4;

    /**
     * 频率1
     */
    private String rate1;

    /**
     * 频率2
     */
    private String rate2;

    /**
     * 频率3
     */
    private String rate3;

    /**
     * 频率4
     */
    private String rate4;

    /**
     * 脉冲宽度1
     */
    private String pulse1;

    /**
     * 脉冲宽度2
     */
    private String pulse2;

    /**
     * 脉冲宽度3
     */
    private String pulse3;

    /**
     * 脉冲宽度4
     */
    private String pulse4;

    /**
     * 脉冲宽度5
     */
    private String pulse5;

    /**
     * 脉冲宽度6
     */
    private String pulse6;

    /**
     * 时长1
     */
    private String time1;

    /**
     * 时长2
     */
    private String time2;

    /**
     * 时长3
     */
    private String time3;

    /**
     * 真假刺激 0真 1假
     */
    private String isExcite;

    /**
     * 预留
     */
    private String extraPack;
}
