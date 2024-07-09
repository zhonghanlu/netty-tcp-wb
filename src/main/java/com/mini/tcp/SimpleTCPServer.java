package com.mini.tcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.coyote.http11.Constants.a;

/**
 * @author zhl
 * @create 2024/7/8 9:56
 */
public class SimpleTCPServer {

    private static final List<Socket> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static Random random = new Random();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static byte[] bytes = null;

    static {
        MessageHeader report = new MessageHeader();
        report.setCommand((byte) 0xAA);
        report.setHospNo((short) 1);
        report.setDeviceNo((byte) 1);
        report.setRunningState((byte) 1);
        report.setExciteType((byte) 11);
        report.setElectricity((short) 11);
        report.setResistance((short) 1);
        report.setBattery((short) 11);
        report.setSignal((short) 1);
        report.setResidueTime((byte) 11);
        report.setExtra(0);
        report.setVerify((byte) 11);

        // 将DeviceReport对象转换为字节数组
        ByteBuf byteBuf = Unpooled.buffer(20);
        byteBuf.writeByte(report.getCommand());
        byteBuf.writeShort(report.getHospNo());
        byteBuf.writeByte(report.getDeviceNo());
        byteBuf.writeByte(report.getRunningState());
        byteBuf.writeByte(report.getExciteType());
        byteBuf.writeShort(report.getElectricity());
        byteBuf.writeShort(report.getResistance());
        byteBuf.writeShort(report.getBattery());
        byteBuf.writeShort(report.getSignal());
        byteBuf.writeByte(report.getResidueTime());
        byteBuf.writeInt(report.getExtra());
        byteBuf.writeByte(report.getVerify());

        // 打印生成的字节数组
        bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        for (byte b : bytes) {
            System.out.printf("%02X ", b);
        }
        System.out.println(bytes.length);
    }

    public static void main(String[] args) throws IOException {
        int port = 18888; // 选择一个端口号
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("TCP模拟服务器启动，正在监听端口: " + port);


        // 启动定时任务，每5秒发送一次消息
        scheduler.scheduleAtFixedRate(() -> {
//            Map<String, Integer> map = new HashMap<String, Integer>() {{
//                put("123", random.nextInt(99999) + 1);
//                put("456", random.nextInt(99999) + 1);
//                put("678", random.nextInt(99999) + 1);
//                put("111", random.nextInt(99999) + 1);
//            }};
//            bytes
            pushMessageToAll("1111");
        }, 0, 5, TimeUnit.SECONDS);

        // 监听并接受连接
        Socket clientSocket = serverSocket.accept();
        System.out.println("TCP模拟服务器连接来自: " + clientSocket.getInetAddress().getHostAddress());

        // 添加客户端到列表
        connectedClients.add(clientSocket);

        while (true) {
            DataInputStream in = new DataInputStream(connectedClients.get(0).getInputStream());
            byte[] buffer = new byte[1024]; // 假设消息长度不超过1024字节
            int readBytes = in.read(buffer);
            String str = new String(buffer, 0, readBytes);
            System.out.println("TCP模拟服务器收到信息: " + str);
        }
    }

    private static void pushMessageToAll(String message) {
        for (Socket client : connectedClients) {
            try {
                OutputStream out = client.getOutputStream();
                out.write(message.getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                // 如果发生异常，移除该客户端
                connectedClients.remove(client);
                try {
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
