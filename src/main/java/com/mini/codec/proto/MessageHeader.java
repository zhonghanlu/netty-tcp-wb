package com.mini.codec.proto;

import lombok.Data;

/**
 * @author zhl
 */
@Data
public class MessageHeader {

    /**
     * 设备编号
     * 1 字节
     */
    private byte deviceNo;

    /**
     * 0xAA
     * 1字节
     */
    private byte flag;

    /**
     * 医院
     * 编号高
     * 1 字节
     */
    private byte hospNoH;

    /**
     * 医院
     * 编号低
     * 1 字节
     */
    private byte hospNoL;

    /**
     * 设备编号保留
     * 1 字节
     */
    private byte deviceNoExtra;

    /**
     * 运行
     * 状态
     * 1 字节
     * 0 停止 1 运行 2暂停
     */
    private byte runningState;

    /**
     * 刺激
     * 类型
     * 1 字节
     * 0:TDCS;1:TPCS;2:TACS;3:TRNS
     */
    private byte exciteType;

    /**
     * 实测
     * 电流 高 存在负数
     * 1 字节
     */
    private byte electricityH;

    /**
     * 实测
     * 电流 低 存在负数
     * 1 字节
     */
    private byte electricityL;

    /**
     * 实测
     * 阻抗 高
     * 1 字节
     */
    private byte resistanceH;

    /**
     * 实测
     * 阻抗 低
     * 1 字节
     */
    private byte resistanceL;

    /**
     * 电池
     * 电量百分比
     * 1 字节
     */
    private byte battery;

    /**
     * 信号强度
     * 1 字节
     */
    private byte signal;

    /**
     * 剩余时间 高
     * 1 字节
     */
    private byte residueTimeH;

    /**
     * 剩余时间 低
     * 1 字节
     */
    private byte residueTimeL;

    /**
     * 保留字段
     * 1 字节
     */
    private int extraA;

    /**
     * 保留字段
     * 1 字节
     */
    private int extraB;

    /**
     * 保留字段
     * 1 字节
     */
    private int extraC;

    /**
     * 保留字段
     * 1 字节
     */
    private int extraD;

    /**
     * 校验字段
     * 1 字节
     */
    private byte verify;
}
