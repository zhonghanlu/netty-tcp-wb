package com.mini.netty.client.handler;

import com.mini.codec.proto.Message;
import com.mini.netty.utils.TcpSocketHolder;
import com.mini.netty.websocket.handler.WebSocketServerHandler;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class NettyTcpClientHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 当通道激活时，可以发送初始化消息
        TcpSocketHolder.add(ctx.channel());
        log.info("连接TCP服务端成功");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 通过解码器，将byteBuf字节数据转换为实体， 回执给websocket，响应给前端
        log.info("收到TCP消息客户端消息：{}", msg);
        WebSocketServerHandler.sendToWebSocket(String.valueOf(msg.getMessageHeader().getDeviceNo()), msg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        TcpSocketHolder.del();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        TcpSocketHolder.del();
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 根据当前连接的channel发送，因为目前就一个TCP服务端
     * <p>
     * 将websocket消息，回传给TCP服务端-》相对当前TCP客户端的服务端
     * websocket消息通过TextWebSocketFrame 转为实体，在通过TCP客户端的编码将实体转为byteBuf
     */
    public static void sendToTcpServer(String data) {
        Channel channel = TcpSocketHolder.getChannel();
        if (Objects.nonNull(channel)) {
            ChannelFuture future = channel.writeAndFlush(data);
            future.addListener((ChannelFutureListener) future1 -> {
                if (!future1.isSuccess()) {
                    log.error("TCP 客户端消息发送失败：{}", future1.cause().getMessage());
                }
            });
        }
    }
}
