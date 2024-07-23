package com.mini.codec.proto;

import lombok.Data;

/**
 * @author zhl
 * @create 2024/7/23 9:15
 */
@Data
public class MessageDTO {

    /**
     * 操作编码
     */
    private String command;

    /**
     * 刺激类型 (A:TDCS;B:TPCS;C:TACS;D:TRNS)
     */
    private String exciteTypePack;

    /**
     * 方向 0正 1负
     */
    private String direction;

    /**
     *  TODO 设备编号
     * 1 字节
     */
    private String deviceNo;

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
