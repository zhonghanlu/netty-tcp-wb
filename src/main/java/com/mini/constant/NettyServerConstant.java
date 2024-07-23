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
}
