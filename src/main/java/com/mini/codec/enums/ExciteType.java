package com.mini.codec.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author zhl
 * @create 2024/7/22 8:54
 */
@Getter
public enum ExciteType {

    // 0:TDCS;1:TPCS;2:TACS;3:TRNS

    /**
     * TDCS
     */
    TDCS(0, "TDCS"),

    /**
     * TPCS
     */
    TPCS(1, "TPCS"),

    /**
     * TACS
     */
    TACS(2, "TACS"),

    /**
     * TRNS
     */
    TRNS(3, "TRNS");

    private int code;

    private String value;

    ExciteType(int code, String value) {
        this.code = code;
        this.value = value;
    }

    @JsonValue
    public String getStringValue() {
        return value;
    }

    @JsonValue
    public String getIntValue() {
        return String.valueOf(code);
    }

    // 静态方法，根据字符串值获取枚举
    public static String fromStringValue(String value) {
        for (ExciteType status : values()) {
            if (status.getIntValue().equals(value)) {
                return status.getStringValue();
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
