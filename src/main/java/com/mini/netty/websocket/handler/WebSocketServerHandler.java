package com.mini.netty.websocket.handler;

import com.mini.netty.websocket.config.NettyConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.mini.netty.constant.WebSocketConstant.WEB_SOCKET_CLIENT_ID;
import static com.mini.netty.constant.WebSocketConstant.WEB_SOCKET_LINK;
import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

@Slf4j
@Component
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String clientId = extractClientIdFromRequest((FullHttpRequest) msg);
            // 重置地址
            request.setUri(WEB_SOCKET_LINK);
            // 获取用户ID,关联channel
            NettyConfig.getUserChannelMap().put(clientId, ctx.channel());
            // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
            AttributeKey<String> key = AttributeKey.valueOf(WEB_SOCKET_CLIENT_ID);
            ctx.channel().attr(key).setIfAbsent(clientId);
            // 回复消息
            ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器连接成功！"));
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        log.info("服务器收到消息：{}", msg.text());
    }

    /**
     * 一但连接第一调用
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("handlerAdded 被调用" + ctx.channel().id().asLongText());
        // 添加到channelGroup 通道组
        NettyConfig.getChannelGroup().add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("handlerRemoved 被调用" + ctx.channel().id().asLongText());
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeClientId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常：{}", cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeClientId(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     */
    private void removeClientId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf(WEB_SOCKET_CLIENT_ID);
        String userId = ctx.channel().attr(key).get();
        NettyConfig.getUserChannelMap().remove(userId);
    }

    /**
     * 处理请求
     */
    private String extractClientIdFromRequest(FullHttpRequest request) {
        // 解析请求的URI以提取查询参数
        String uri = request.uri();
        int queryStartIndex = uri.indexOf('?');
        if (queryStartIndex > -1) {
            String query = uri.substring(queryStartIndex + 1);
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(WEB_SOCKET_CLIENT_ID)) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    /**
     * 根据注册clientId单发
     */
    public static void sendToWebSocket(String clientId, String data) {
        Channel channel = NettyConfig.getUserChannelMap().get(clientId);
        if (Objects.nonNull(channel)) {
            log.info("发送消息给客户端：{}：{}", clientId, data);
            channel.writeAndFlush(new TextWebSocketFrame(data));
        }
    }

}

