package com.mini.codec.proto;

import com.mini.codec.enums.ExciteType;
import com.mini.codec.enums.RunOrStop;
import lombok.Data;

/**
 * @author zhl
 * @create 2024/7/19 16:43
 */
@Data
public class MessageVo {

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 医院
     * 编号高
     */
    private String hospNo;

    /**
     * 运行
     * 状态
     * 0 停止 1 运行 2暂停
     */
    private String runningState;

    /**
     * 刺激
     * 类型
     * 0:TDCS;1:TPCS;2:TACS;3:TRNS
     */
    private String exciteType;

    /**
     * 实测
     * 电流  存在负数
     */
    private String electricity;

    /**
     * 实测
     * 阻抗
     */
    private String resistance;

    /**
     * 电池
     * 电量百分比
     */
    private String battery;

    /**
     * 信号强度
     */
    private String signal;

    /**
     * 剩余时间
     */
    private String residueTime;

    /**
     * 保留字段
     * 1 字节
     */
    private String extraA;

    /**
     * 保留字段
     * 1 字节
     */
    private String extraB;

    /**
     * 保留字段
     * 1 字节
     */
    private String extraC;

    /**
     * 保留字段
     * 1 字节
     */
    private String extraD;

    /**
     * 校验字段
     * 1 字节
     */
    private String verify;

}
