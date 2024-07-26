package com.mini.netty.server.handler;

import com.mini.NettyProperties;
import com.mini.codec.enums.Command;
import com.mini.codec.enums.RunOrStop;
import com.mini.codec.proto.Message;
import com.mini.codec.proto.MessageHeader;
import com.mini.codec.proto.MessagePack;
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
import java.util.concurrent.TimeUnit;

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

    /**
     * 接收硬件对接消息：
     * 1.正常接收数据处理
     * 2.服务端主动发送消息，客户端回执ack消息处理
     * 3.设计
     *      3.1.操作命令缓存
     *      3.2.机器稳定倒计时缓存，销毁时间，无需主动清除
     *      3.3.任务时效缓存
     *      3.4.任务最大电阻缓存
     * 4.接收启动命令，处理操作命令缓存，以及机器稳定倒计时缓存
     * 5.接收到暂停命令，处理机器倒计时缓存续期
     * 6.启动命令，以及操作命令存在，并且倒计时缓存不存在时记录时效时间以及实时电阻
     * 6.1.达到预定阙值时进行暂停命令，再次进入进行倒计时缓存续期
     * 7.接收到停止命令，进行时效时间统计以及最大阻抗统计，调用db操作，清空缓存，代表此通道的此任务结束
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 剔除ack指令，暂且不需要此指令
        MessageHeader messageHeader = msg.getMessageHeader();
        if (0 == messageHeader.getRunningState()
            && 0 == messageHeader.getExciteType()
            && 0 == messageHeader.getBattery()
            && 0 == messageHeader.getSignal()
            && 0 == messageHeader.getVerify()) {
            log.info("客户端操作，回执消息，无需处理逻辑");
            return;
        }

        // 通过解码器，将byteBuf字节数据转换为实体， 回执给websocket，响应给前端
        WebSocketServerHandler.sendToWebSocket(String.valueOf(messageHeader.getDeviceNo()), msg);

        MessageVo messageVo = msg.getMessageVo();
        String deviceNo = messageVo.getDeviceNo();
        String channelId = ctx.channel().id().asLongText();
        String runStatus = String.valueOf(msg.getMessageHeader().getRunningState());

        // channel 缓存状态key
        String channelCommandKey = NettyServerConstant.CHANNEL_RELATION_COMMAND + channelId;
        // 机器启动稳定倒计时
        String countDownKey = NettyServerConstant.CHANNEL_RESISTANCE_COUNT_DOWN + channelId;

        // 15秒已过，稳定进行记录
        Object countDown = RedisUtils.getCacheObject(countDownKey);
        // 检测当前channel对的机器是否进行启动
        String command = RedisUtils.getCacheObject(channelCommandKey);

        // 检测表示flag
        boolean flag = StringUtils.isNotEmpty(command) && command.equals(Command.BB.getStringValue());

        // 启动,键入启动缓存操作
        if (RunOrStop.RUN.getIntValue().equals(runStatus) && StringUtils.isEmpty(command)) {
            // 存入启动缓存指令
            RedisUtils.setCacheObject(channelCommandKey, Command.BB.getStringValue());
            log.info("当前ChannelId为:{},执行{}操作", channelId, Command.BB.getStringValue());

            // 设置一个15秒阻抗稳定时长
            RedisUtils.setCacheObject(countDownKey, channelId, NettyServerConstant.CHANNEL_RESISTANCE_COUNT_DOWN_EXPIRE, TimeUnit.SECONDS);
        }

        // 暂停，持续为countDownKey进行续期
        if (RunOrStop.SUSPENDED.getIntValue().equals(runStatus)) {
            // 设置一个15秒阻抗稳定时长
            RedisUtils.setCacheObject(countDownKey, channelId, NettyServerConstant.CHANNEL_RESISTANCE_COUNT_DOWN_EXPIRE, TimeUnit.SECONDS);
        }

        // 已启动且缓存中有启动信息，将此次实时信息存入
        if (RunOrStop.RUN.getIntValue().equals(runStatus) && Objects.isNull(countDown) && flag) {
            // 电阻 根据ZSet存储
            String resistance = messageVo.getResistance();
            String resistanceKey = NettyServerConstant.CHANNEL_RELATION_RESISTANCE + channelId;
            RedisUtils.setZSetObject(resistanceKey, messageVo, Double.parseDouble(resistance));
            // 所用耗时 根据ZSet
            String resistanceTimeKey = NettyServerConstant.CHANNEL_RELATION_RESIDUE_TIME + channelId;
            RedisUtils.setZSetObject(resistanceTimeKey, messageVo, Double.parseDouble(messageVo.getResidueTime()));

            // 获取当前阻抗，进行处理，大于阙值，进行暂停操作
            if (Math.round(Float.parseFloat(resistance)) > NettyProperties.TCP_SERVER_RESISTANCE_RECEIVE_MAX) {
                // 暂停
                Message message = new Message();
                MessagePack<Object> pack = new MessagePack<>();
                pack.setOptCommand(new MessagePack.OptCommand(Command.PS.getStringValue(), deviceNo));
                message.setMessagePack(pack);
                sendToTcpServer(deviceNo, message);
            }
        }

        // 已完成 或者主动停止状态，随即清除所有此次任务所有缓存信息
        if (RunOrStop.STOP.getIntValue().equals(runStatus) && flag) {
            String resistanceKey = NettyServerConstant.CHANNEL_RELATION_RESISTANCE + channelId;
            // 最大阻抗
            double resistanceLargest = RedisUtils.getZSetLastScore(resistanceKey);

            // 使用时长
            String resistanceTimeKey = NettyServerConstant.CHANNEL_RELATION_RESIDUE_TIME + channelId;

            double resistanceTimeLargest = RedisUtils.getZSetLastScore(resistanceTimeKey);
            double resistanceTimeMini = RedisUtils.getZSetFirstScore(resistanceTimeKey);
            double resistanceTime = resistanceTimeLargest - resistanceTimeMini;

            // 处理业务操作
            log.info("执行时间：{},最大电阻：{}", resistanceTime, resistanceLargest);
//            new SpecialSubmissionsResult().submitStimulation(deviceNo, Math.round(resistanceTime), String.valueOf(resistanceLargest));

            // 删除此channel的所有缓存信息
            RedisUtils.deleteObject(resistanceKey);  // 启动之后阻抗信息
            RedisUtils.deleteObject(resistanceTimeKey); // 启动之后时效时间信息
            RedisUtils.deleteObject(channelCommandKey); // 启动操作启动信息
        }
    }

    /**
     * 断开连接调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        log.info("客戶端主动断开，channelId:{}", channelId);
        // 状态
        String channelCommandKey = NettyServerConstant.CHANNEL_RELATION_COMMAND + channelId;
        // 最大阻抗
        String resistanceKey = NettyServerConstant.CHANNEL_RELATION_RESISTANCE + channelId;
        // 使用时长
        String resistanceTimeKey = NettyServerConstant.CHANNEL_RELATION_RESIDUE_TIME + channelId;
        // 删除此channel的所有缓存信息
        RedisUtils.deleteObject(resistanceKey);
        RedisUtils.deleteObject(resistanceTimeKey);
        RedisUtils.deleteObject(channelCommandKey);
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
    public static String sendToTcpServer(String clientId, Message data) {
        Channel channel = TcpSocketHolder.getUserChannelMap().get(clientId);

        if (Objects.isNull(channel)) {
            log.error("当前clientId，没有查询到对应channel");
            return "当前clientId，没有查询到对应channel";
        }

        ChannelFuture future = channel.writeAndFlush(data);
        future.addListener((ChannelFutureListener) future1 -> {
            if (!future1.isSuccess()) {
                log.error("TCP 服务端消息发送失败：{}", future1.cause().getMessage());
                // 表示此channel已有误，执行删除操作
                TcpSocketHolder.getUserChannelMap().remove(clientId);
            }
        });
        return null;
    }

    /**
     * 检测当前阻抗是否大于阙值
     */
    private static String checkRealResistance(String channelId, Message data) {
        String command = data.getMessagePack().getOptCommand().getCommand();
        String realResistanceKey = NettyServerConstant.CHANNEL_ALL_RESISTANCE + channelId;
        if (Command.BB.getStringValue().equals(command)) {
            String realResistance = RedisUtils.getCacheObject(realResistanceKey);

            if (Math.round(Float.parseFloat(realResistance)) > NettyProperties.TCP_SERVER_RESISTANCE_RECEIVE_MAX) {
                return "当前机器阻抗大于最高阻抗，不允许启动，请稍后重试";
            }
        }
        return null;
    }
}
