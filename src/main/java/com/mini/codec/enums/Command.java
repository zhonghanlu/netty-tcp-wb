package com.mini.codec.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author zhl
 * @create 2024/7/23 9:08
 */
public enum Command {

    /**
     * 启动命令：“BB”
     * 停止命令：“SS”
     * 暂停命令：“PS”
     * 取消暂停命令：“PB”
     * 关机命令：“ZZ”
     * 数据重发命令：“RR”
     * 允许手动命令：“HA” +“1”或“0”
     * 蜂鸣器打开命令：“BP”+“1”或“0”
     */
    C("C"),
    BB("BB"),
    SS("SS"),
    PS("PS"),
    PB("PB"),
    ZZ("ZZ"),
    RR("RR"),
    HA1("HA1"),
    HA0("HA0"),
    BP1("BP1"),
    BP0("BP0");

    Command(String value) {
        this.value = value;
    }

    private String value;

    @JsonValue
    public String getStringValue() {
        return value;
    }
}
