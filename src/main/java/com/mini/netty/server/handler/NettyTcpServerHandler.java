package com.mini.netty.server.handler;

import com.mini.codec.proto.Message;
import com.mini.netty.utils.TcpSocketHolder;
import com.mini.netty.websocket.handler.WebSocketServerHandler;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.mini.constant.NettyServerConstant.TCP_SOCKET_CLIENT_ID;

@Slf4j
@Component
public class NettyTcpServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        String deviceNo = String.valueOf(message.getMessageHeader().getDeviceNo());
        // 获取用户ID,关联channel
        // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
        TcpSocketHolder.getUserChannelSetMap().computeIfAbsent(deviceNo, k -> new HashSet<>()).add(ctx.channel());
        AttributeKey<String> key = AttributeKey.valueOf(TCP_SOCKET_CLIENT_ID);
        ctx.channel().attr(key).setIfAbsent(deviceNo);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 当通道激活时，可以发送初始化消息
        TcpSocketHolder.getChannelGroup().add(ctx.channel());
        log.info("连接TCP服务端成功");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 通过解码器，将byteBuf字节数据转换为实体， 回执给websocket，响应给前端
//        log.debug("收到TCP消息客户端消息：{}", msg);
        WebSocketServerHandler.sendToWebSocket(String.valueOf(msg.getMessageHeader().getDeviceNo()), msg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        TcpSocketHolder.getChannelGroup().remove(ctx.channel());
        removeClientId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        TcpSocketHolder.getChannelGroup().remove(ctx.channel());
        removeClientId(ctx);
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     */
    private void removeClientId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf(TCP_SOCKET_CLIENT_ID);
        String userId = ctx.channel().attr(key).get();
        if (!TcpSocketHolder.getUserChannelSetMap().get(userId).isEmpty()) {
            TcpSocketHolder.getUserChannelSetMap().remove(userId);
        }
    }

    /**
     * 根据当前连接的channel发送，因为目前就一个TCP服务端
     * <p>
     * 将websocket消息，回传给TCP服务端-》相对当前TCP客户端的服务端
     * websocket消息通过TextWebSocketFrame 转为实体，在通过TCP客户端的编码将实体转为byteBuf
     */
    public static void sendToTcpServer(String clientId, Message data) {
        Set<Channel> channelSet = TcpSocketHolder.getUserChannelSetMap().get(clientId);
        if (Objects.nonNull(channelSet) && !channelSet.isEmpty()) {
            channelSet.forEach(channel -> {
                ChannelFuture future = channel.writeAndFlush(data);
                future.addListener((ChannelFutureListener) future1 -> {
                    if (!future1.isSuccess()) {
                        log.error("TCP 服务端消息发送失败：{}", future1.cause().getMessage());
                        // 表示此channel已有误，执行删除操作
                        channelSet.remove(channel);
                    }
                });
            });
        }
    }
}
