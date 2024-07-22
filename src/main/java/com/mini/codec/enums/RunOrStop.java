package com.mini.codec.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author zhl
 * @create 2024/7/22 8:48
 */
@Getter
public enum RunOrStop {

    //0 停止 1 运行 2暂停

    /**
     * 运行
     */
    RUN(1, "RUN"),

    /**
     * 停止
     */
    STOP(0, "STOP"),

    /**
     * 暂停
     */
    SUSPENDED(2, "SUSPENDED");

    private int code;

    private String value;

    RunOrStop(int code, String value) {
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
        for (RunOrStop status : values()) {
            if (status.getIntValue().equals(value)) {
                return status.getStringValue();
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

}
