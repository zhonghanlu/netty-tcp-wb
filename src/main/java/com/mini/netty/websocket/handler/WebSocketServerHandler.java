package com.mini.netty.websocket.handler;

import com.mini.NettyProperties;
import com.mini.codec.proto.Message;
import com.mini.codec.proto.MessageScale;
import com.mini.netty.server.handler.NettyTcpServerHandler;
import com.mini.netty.utils.WebSocketHolder;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.mini.constant.NettyServerConstant.WEB_SOCKET_CLIENT_ID;
import static com.mini.constant.NettyServerConstant.WEB_SOCKET_SCALE_ID;


@Slf4j
@Component
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            Map<String, String> paramMap = extractClientIdFromRequest((FullHttpRequest) msg);
            AtomicReference<String> targetKey = new AtomicReference<>("");
            paramMap.keySet().forEach(k -> {
                if (k.equals(WEB_SOCKET_CLIENT_ID)) {
                    targetKey.set(WEB_SOCKET_CLIENT_ID);
                } else if (k.equals(WEB_SOCKET_SCALE_ID)) {
                    targetKey.set(WEB_SOCKET_SCALE_ID);
                }
            });

            // 重置地址
            if (StringUtils.isEmpty(targetKey.get())) {
                return;
            }

            request.setUri(NettyProperties.WEB_SOCKET_PREFIX);

            String unionKey = paramMap.get(targetKey.get());
            // 获取用户ID,关联channel
            // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
            WebSocketHolder.getUserChannelSetMap().computeIfAbsent(unionKey, k -> new HashSet<>()).add(ctx.channel());
            AttributeKey<String> key = AttributeKey.valueOf(targetKey.get());
            ctx.channel().attr(key).setIfAbsent(unionKey);
            // 回复消息
            ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器连接成功！"));
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (Objects.nonNull(msg.getMessageScale())) {
            sendToWebSocket(msg.getMessageScale().getCode(), msg);
        } else {
            // 发送给TCP服务端，根据指令区分操作类型，
            NettyTcpServerHandler.sendToTcpServer(msg.getMessagePack().getOptCommand().getClientId(), msg);
        }
    }


    /**
     * 一但连接第一调用
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // 添加到channelGroup 通道组
        WebSocketHolder.getChannelGroup().add(ctx.channel());
        AttributeKey<String> key = AttributeKey.valueOf(WEB_SOCKET_SCALE_ID);
        String clientId = ctx.channel().attr(key).get();
        //广播通知此端已下线
        Message message = new Message();
        MessageScale ms = new MessageScale();
        ms.setWorkCode(clientId);
        ms.setCommand("ON");
        ms.setCode(clientId);
        message.setMessageScale(ms);
        sendToWebSocket("D0001", message);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
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
        List<String> clientIdList = Arrays.asList(WEB_SOCKET_CLIENT_ID, WEB_SOCKET_SCALE_ID);
        clientIdList.forEach(client -> {
            AttributeKey<String> key = AttributeKey.valueOf(client);
            String clientId = ctx.channel().attr(key).get();
            if (StringUtils.isNotEmpty(clientId)) {
                Set<Channel> channelSet = WebSocketHolder.getUserChannelSetMap().get(clientId);
                if (Objects.nonNull(channelSet) && !CollectionUtils.isEmpty(channelSet)) {
                    WebSocketHolder.getUserChannelSetMap().remove(clientId);
                    //广播通知此端已下线
                    Message message = new Message();
                    MessageScale ms = new MessageScale();
                    ms.setWorkCode(clientId);
                    ms.setCommand("OFF");
                    ms.setCode(clientId);
                    message.setMessageScale(ms);
                    sendToWebSocket("D0001", message);
                }
            }
        });
    }

    /**
     * 处理请求
     */
    private Map<String, String> extractClientIdFromRequest(FullHttpRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        // 解析请求的URI以提取查询参数
        String uri = request.uri();
        int queryStartIndex = uri.indexOf('?');
        if (queryStartIndex > -1) {
            String query = uri.substring(queryStartIndex + 1);
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    paramMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return paramMap;
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

