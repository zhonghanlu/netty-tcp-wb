package com.mini.constant;

/**
 * @author zhl
 * @create 2024/7/8 18:52
 */
public class NettyServerConstant {

    public NettyServerConstant() {
    }

    /**
     * 客户端请求地址
     */
    public static final String WEB_SOCKET_LINK = "/ws";

    /**
     * 客户端id
     */
    public static final String WEB_SOCKET_CLIENT_ID = "clientId";

    /**
     * TCP Client id
     */
    public static final String TCP_SOCKET_CLIENT_ID = "clientId";

    /**
     * webSocket协议名
     */
    public static final String WEBSOCKET_PROTOCOL = "WebSocket";


    /**
     * Tcp to send message
     */
    public static final String TCP_MESSAGE_ZERO_PADDING = "00000000000000000000";


    ////////////////////////////////////Redis channel relation/////////////////////////////////////////////
    /**
     * Tcp to send message
     */
    public static final String CHANNEL_RELATION_COMMAND = "channel_relation_command:";

    /**
     * Tcp to send message 阻抗
     */
    public static final String CHANNEL_RELATION_RESISTANCE = "channel_relation_resistance:";

    /**
     * Tcp to send 时效时间
     */
    public static final String CHANNEL_RELATION_RESIDUE_TIME = "channel_relation_residueTime:";

    /**
     * Tcp 全局阻抗信息
     */
    public static final String CHANNEL_ALL_RESISTANCE = "channel_all_resistance:";

    /**
     * 机器启动倒计时
     */
    public static final String CHANNEL_RESISTANCE_COUNT_DOWN = "channel_resistance_count_down:";

    /**
     * 机器结束倒计时
     */
    public static final String CHANNEL_RESISTANCE_COUNT_DOWN_END = "channel_resistance_count_down_end:";

    /**
     * 过期时间
     */
    public static final int CHANNEL_RESISTANCE_COUNT_DOWN_EXPIRE = 15;


    /**
     * 训练端Id
     */
    public static final String WEB_SOCKET_SCALE_ID = "code";
}
