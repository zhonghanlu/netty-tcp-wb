package com.mini.netty.websocket.handler;

import com.mini.codec.proto.Message;
import com.mini.netty.client.handler.NettyTcpClientHandler;
import com.mini.netty.utils.WebSocketHolder;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.mini.common.constant.NettyServerConstant.WEB_SOCKET_CLIENT_ID;
import static com.mini.common.constant.NettyServerConstant.WEB_SOCKET_LINK;


@Slf4j
@Component
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String clientId = extractClientIdFromRequest((FullHttpRequest) msg);
            // 重置地址
            request.setUri(WEB_SOCKET_LINK);
            // 获取用户ID,关联channel
            // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
            WebSocketHolder.getUserChannelSetMap().computeIfAbsent(clientId, k -> new HashSet<>()).add(ctx.channel());
            AttributeKey<String> key = AttributeKey.valueOf(WEB_SOCKET_CLIENT_ID);
            ctx.channel().attr(key).setIfAbsent(clientId);
            // 回复消息
            ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器连接成功！"));
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 发送给TCP服务端，根据指令区分操作类型，
        // 1.开关暂停指令 TODO：定制化操作硬件
        NettyTcpClientHandler.sendToTcpServer(msg);
//        log.info("websocket服务器收到消息：{}", msg);
    }

    /**
     * 一但连接第一调用
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
//        log.info("handlerAdded 被调用" + ctx.channel().id().asLongText());
        // 添加到channelGroup 通道组
        WebSocketHolder.getChannelGroup().add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
//        log.info("handlerRemoved 被调用" + ctx.channel().id().asLongText());
        WebSocketHolder.getChannelGroup().remove(ctx.channel());
        removeClientId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("异常：{}", cause.getMessage());
        // 删除通道
        WebSocketHolder.getChannelGroup().remove(ctx.channel());
        removeClientId(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     */
    private void removeClientId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf(WEB_SOCKET_CLIENT_ID);
        String userId = ctx.channel().attr(key).get();
        WebSocketHolder.getUserChannelSetMap().get(userId).remove(ctx.channel());
        if (WebSocketHolder.getUserChannelSetMap().get(userId).isEmpty()) {
            WebSocketHolder.getUserChannelSetMap().remove(userId);
        }
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
     * 通过解码器，将message信息转换为TextWebSocketFrame JSON
     */
    public static void sendToWebSocket(String clientId, Message message) {
        Set<Channel> channelSet = WebSocketHolder.getUserChannelSetMap().get(clientId);
        if (!CollectionUtils.isEmpty(channelSet) && Objects.nonNull(message)) {
            channelSet.forEach(channel -> {
                ChannelFuture future = channel.writeAndFlush(message);

                future.addListener((ChannelFutureListener) future1 -> {
                    if (!future1.isSuccess()) {
                        log.error("websocket消息发送失败:{} clientId:{} Message:{}", future1.cause().getMessage(), clientId, message);
                    }
                });
            });
        }
    }

}

