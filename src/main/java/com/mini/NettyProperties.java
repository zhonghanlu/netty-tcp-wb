package com.mini;

import com.mini.netty.utils.IPUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @author zhl
 * @create 2024/7/11 15:02
 * @description 先暂且这么多，后续再补充
 */
@Data
@Component
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

    /**
     * tcp服务端地址
     */
    private String tcpServerHost = "";

    public static String TCP_SERVER_HOST;

    @PostConstruct
    public void setTcpServerHost() {
        if (StringUtils.isEmpty(this.tcpServerHost)) {
            this.tcpServerHost = IPUtils.getIp();
        }
        TCP_SERVER_HOST = this.tcpServerHost;
    }

    /**
     * 获取本机ip
     */
    public String getTcpServerHost() {
        return TCP_SERVER_HOST;
    }

    /**
     * tcp服务端端口
     */
    private int tcpServerPort = 1888;

    public static int TCP_SERVER_PORT;

    @PostConstruct
    public void setTcpServerPort() {
        TCP_SERVER_PORT = this.tcpServerPort;
    }

    public int getTcpServerPort() {
        return TCP_SERVER_PORT;
    }

    /**
     * tcp服务端是否开启
     */
    private boolean tcpServerEnabled = true;

    public static boolean TCP_SERVER_ENABLED;

    @PostConstruct
    public void setTcpClientEnabled() {
        TCP_SERVER_ENABLED = this.tcpServerEnabled;
    }

    public boolean getTcpClientEnabled() {
        return TCP_SERVER_ENABLED;
    }

    /**
     * tcp服务端是否开启断线重连
     */
    private boolean tcpClientRetryingEnabled = true;

    public static boolean TCP_CLIENT_RETRYING_ENABLED;

    @PostConstruct
    public void setTcpServerRetryingEnabled() {
        TCP_CLIENT_RETRYING_ENABLED = this.tcpClientRetryingEnabled;
    }

    public boolean getTcpServerRetryingEnabled() {
        return TCP_CLIENT_RETRYING_ENABLED;
    }

    /**
     * tcp服务端断线重连频率
     */
    private int tcpClientRetryingInterval = 3;

    public static int TCP_CLIENT_RETRYING_INTERVAL;

    @PostConstruct
    public void setTcpServerRetryingTimes() {
        TCP_CLIENT_RETRYING_INTERVAL = this.tcpClientRetryingInterval;
    }

    public int getTcpServerRetryingTimes() {
        return TCP_CLIENT_RETRYING_INTERVAL;
    }

    // =====================分割线======================


    /**
     * websocket端口
     */
    private int webSocketPort = 18080;

    public static int WEB_SOCKET_PORT;

    @PostConstruct
    public void setWebSocketPort() {
        WEB_SOCKET_PORT = this.webSocketPort;
    }

    public int getWebSocketPort() {
        return WEB_SOCKET_PORT;
    }

    /**
     * websocket服务是否开启
     */
    private boolean webSocketEnabled = true;

    public static boolean WEB_SOCKET_ENABLED;

    @PostConstruct
    public void setWebSocketEnabled() {
        WEB_SOCKET_ENABLED = this.webSocketEnabled;
    }

    public boolean getWebSocketEnabled() {
        return WEB_SOCKET_ENABLED;
    }

    /**
     * websocket服务前缀
     */
    private String webSocketPrefix = "/ws";

    public static String WEB_SOCKET_PREFIX;

    @PostConstruct
    public void setWebSocketPrefix() {
        WEB_SOCKET_PREFIX = this.webSocketPrefix;
    }

    public String getWebSocketPrefix() {
        return WEB_SOCKET_PREFIX;
    }
}
