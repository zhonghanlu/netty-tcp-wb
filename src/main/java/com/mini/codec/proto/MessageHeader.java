package com.mini.codec.proto;

import lombok.Data;

/**
 * @author zhl
 */
@Data
public class MessageHeader {

    /**
     * 包头
     * OxAA
     * 1 字节
     */
    private int command;

    /**
     * 医院
     * 编号
     * 2 字节
     */
    private short hospNo;

    /**
     * 设备编号
     * 1 字节
     */
    private byte deviceNo;

    /**
     * 运行
     * 状态
     * 1 字节
     */
    private byte runningState;

    /**
     * 刺激
     * 类型
     * 1 字节
     */
    private byte exciteType;

    /**
     * 实测
     * 电流
     * 2 字节
     */
    private short electricity;

    /**
     * 实测
     * 阻抗
     * 2 字节
     */
    private short resistance;

    /**
     * 电池
     * 电量
     * 2 字节
     */
    private short battery;

    /**
     * 信号强度
     * 2 字节
     */
    private short signal;

    /**
     * 剩余时间
     * 1 字节
     */
    private byte residueTime;

    /**
     * 保留字段
     * 4 字节
     */
    private int extra;

    /**
     * 校验字段
     * 1 字节
     */
    private byte verify;
}
