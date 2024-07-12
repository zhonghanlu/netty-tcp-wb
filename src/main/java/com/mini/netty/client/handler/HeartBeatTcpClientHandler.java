package com.mini.netty.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HeartBeatTcpClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 事件触发后会调用此方法
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                String content = "客户端写事件触发，向服务端发送心跳包";
                ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
                ctx.writeAndFlush(byteBuf);
            } else if (e.state() == IdleState.READER_IDLE) {
                String content = "客户端读事件触发，向服务端发送心跳包";
                ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
                ctx.writeAndFlush(byteBuf);
            } else if (e.state() == IdleState.ALL_IDLE) {
                String content = "客户端的读/写事件触发，向服务端发送心跳包";
                ByteBuf byteBuf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
                ctx.writeAndFlush(byteBuf);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

