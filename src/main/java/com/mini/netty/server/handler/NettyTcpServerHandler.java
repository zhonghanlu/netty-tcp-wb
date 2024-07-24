package com.mini.netty.server.handler;

import com.mini.codec.enums.Command;
import com.mini.codec.proto.Message;
import com.mini.codec.proto.MessageVo;
import com.mini.constant.NettyServerConstant;
import com.mini.netty.utils.RedisUtils;
import com.mini.netty.utils.TcpSocketHolder;
import com.mini.netty.websocket.handler.WebSocketServerHandler;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        TcpSocketHolder.getUserChannelMap().put(deviceNo, ctx.channel());
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
        // 检测当前channel对的机器是否进行启动
        String channelId = ctx.channel().id().asLongText();
        String redisKey = NettyServerConstant.CHANNEL_RELATION_COMMAND + channelId;
        String command = RedisUtils.getCacheObject(redisKey);
        // 已启动，将此次实时信息存入
        if (StringUtils.isNotBlank(channelId) && command.equals(Command.BB.getStringValue())) {
            MessageVo messageVo = msg.getMessageVo();
            if (Objects.nonNull(messageVo)) {
                // 电阻 根据ZSet存储
                String resistanceKey = NettyServerConstant.CHANNEL_RELATION_RESISTANCE + channelId;
                RedisUtils.setZSetObject(resistanceKey, msg.getMessageVo(), Double.parseDouble(messageVo.getResistance()));
                // 所用耗时 根据ZSet
                String resistanceTimeKey = NettyServerConstant.CHANNEL_RELATION_RESIDUE_TIME + channelId;
                RedisUtils.setZSetObject(resistanceTimeKey, msg.getMessageVo(), Double.parseDouble(messageVo.getResidueTime()));
            }
        }
    }

    /**
     * 断开连接调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客戶端主动断开，channelId:{}", ctx.channel().id().asLongText());

        String channelId = ctx.channel().id().asLongText();
        String resistanceKey = NettyServerConstant.CHANNEL_RELATION_RESISTANCE + channelId;
        // 最大阻抗
        Double resistanceLargest = RedisUtils.getZSetLastScore(resistanceKey);

        // 使用时长
        String resistanceTimeKey = NettyServerConstant.CHANNEL_RELATION_RESIDUE_TIME + channelId;
        Double resistanceTimeLargest = RedisUtils.getZSetLastScore(resistanceTimeKey);
        Double resistanceTimeMini = RedisUtils.getZSetLastScore(resistanceTimeKey);
        double resistanceTime = resistanceTimeLargest - resistanceTimeMini;

        // 处理业务操作
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
        String clientId = ctx.channel().attr(key).get();
        Channel channel = TcpSocketHolder.getUserChannelMap().get(clientId);
        if (Objects.nonNull(channel)) {
            TcpSocketHolder.getUserChannelMap().remove(clientId);
        }
    }

    /**
     * 根据当前连接的channel发送，因为目前就一个TCP服务端
     * <p>
     * 将websocket消息，回传给TCP服务端-》相对当前TCP客户端的服务端
     * websocket消息通过TextWebSocketFrame 转为实体，在通过TCP客户端的编码将实体转为byteBuf
     */
    public static void sendToTcpServer(String clientId, Message data) {
        Channel channel = TcpSocketHolder.getUserChannelMap().get(clientId);

        if (Objects.isNull(channel)) {
            log.error("当前clientId，没有查询到对应channel");
        }

        if (Objects.nonNull(channel)) {
            ChannelFuture future = channel.writeAndFlush(data);
            future.addListener((ChannelFutureListener) future1 -> {
                if (!future1.isSuccess()) {
                    log.error("TCP 服务端消息发送失败：{}", future1.cause().getMessage());
                    // 表示此channel已有误，执行删除操作
                    TcpSocketHolder.getUserChannelMap().remove(clientId);
                }

                // 成功记录当前设备码值，以及对应操作，便于后续进行计算此流程操作
                if (future1.isSuccess()) {
                    // 启动
                    String command = data.getMessagePack().getOptCommand().getCommand();
                    if (Command.BB.getStringValue().equals(command)) {
                        String channelId = channel.id().asLongText();
                        String redisKey = NettyServerConstant.CHANNEL_RELATION_COMMAND + channelId;
                        RedisUtils.setCacheObject(redisKey, command);
                        log.info("当前ChannelId为:{},执行{}操作", channelId, command);
                    }
                }
            });
        }
    }
}
