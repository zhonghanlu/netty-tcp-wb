package com.mini.codec.proto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: zhl
 * @description: 消息服务发送给tcp的包体, tcp再根据改包体解析成Message发给客户端
 **/
@Data
public class MessagePack<T> implements Serializable {

    ////////////////////////////////////hex请求参 无需Encoder/////////////////////////////////////////////

    /**
     * 操作hex
     * <p>
     * 启动命令：“BB”
     * 停止命令：“SS”
     * 暂停命令：“PS”
     * 取消暂停命令：“PB”
     * 关机命令：“ZZ”
     * 数据重发命令：“RR”
     * 允许手动命令：“HA” +“1”或“0”
     * 蜂鸣器打开命令：“BP”+“1”或“0”
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
