package com.mini.netty.utils;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * netty配置信息
 */
public class WebSocketHolder {

    /**
     * 定义一个channel组，管理所有的channel
     * GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
     */
    private static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存放用户与Chanel的对应信息，用于给指定用户发送消息
     */
    private static ConcurrentHashMap<String, Channel> CLIENTS_MAP = new ConcurrentHashMap<>();

    /**
     * 存放用户与Chanel的对应信息，用于给同一个clientId发送消息
     */
    private static ConcurrentHashMap<String, Set<Channel>> CLIENTS_SET_MAP = new ConcurrentHashMap<>();

    private WebSocketHolder() {
    }

    /**
     * 获取channel组
     *
     * @return
     */
    public static ChannelGroup getChannelGroup() {
        return CHANNEL_GROUP;
    }

    /**
     * 获取用户channel map
     *
     * @return
     */
    public static ConcurrentMap<String, Channel> getUserChannelMap() {
        return CLIENTS_MAP;
    }

    /**
     * 获取用户channel map
     */
    public static ConcurrentMap<String, Set<Channel>> getUserChannelSetMap() {
        return CLIENTS_SET_MAP;
    }

}
