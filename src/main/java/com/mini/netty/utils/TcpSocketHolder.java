package com.mini.netty.utils;

import io.netty.channel.Channel;

/**
 * @author zhl
 * @create 2024/7/9 17:39
 */
public class TcpSocketHolder {

    /**
     * 定义一个channel
     */
    private static volatile Channel CHANNEL;


    /**
     * 添加channel
     */
    public static void add(Channel ctx) {
        CHANNEL = ctx;
    }

    /**
     * 删除channel
     */
    public static void del() {
        CHANNEL = null;
    }

    /**
     * 获取channel组
     */
    public static Channel getChannel() {
        return CHANNEL;
    }

}
