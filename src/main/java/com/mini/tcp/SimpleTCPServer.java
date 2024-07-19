//package com.mini.tcp;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mini.codec.proto.MessageHeader;
//import com.mini.codec.proto.MessagePack;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author zhl
// * @create 2024/7/8 9:56
// */
//public class SimpleTCPServer {
//
//    private static final List<Socket> connectedClients = Collections.synchronizedList(new ArrayList<>());
//    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//    private static Random random = new Random();
//    private static ObjectMapper objectMapper = new ObjectMapper();
//
//    private static byte[] bytes = null;
//
//    private static List<byte[]> bytesList = new ArrayList<>();
//
////    private static void genderData() {
////
////        bytesList.add(bytes);
////
////        MessageHeader report1 = new MessageHeader();
////        report1.setCommand((byte) 0xAA);
////        report1.setHospNo((short) 1);
////        report1.setDeviceNo((byte) 123);
////        report1.setRunningState((byte) 1);
////        report1.setExciteType((byte) 11);
////        report1.setElectricity((short) random.nextInt(9));
////        report1.setResistance((short) 1);
////        report1.setBattery((short) 11);
////        report1.setSignal((short) 1);
////        report1.setResidueTime((byte) 11);
////        report1.setExtra(0);
////        report1.setVerify((byte) 11);
////
////        // 将DeviceReport对象转换为字节数组
////        ByteBuf byteBuf1 = Unpooled.buffer(20);
////        byteBuf1.writeByte(report.getCommand());
////        byteBuf1.writeShort(report.getHospNo());
////        byteBuf1.writeByte(report.getDeviceNo());
////        byteBuf1.writeByte(report.getRunningState());
////        byteBuf1.writeByte(report.getExciteType());
////        byteBuf1.writeShort(report.getElectricity());
////        byteBuf1.writeShort(report.getResistance());
////        byteBuf1.writeShort(report.getBattery());
////        byteBuf1.writeShort(report.getSignal());
////        byteBuf1.writeByte(report.getResidueTime());
////        byteBuf1.writeInt(report.getExtra());
////        byteBuf1.writeByte(report.getVerify());
////
////        // 打印生成的字节数组
////        byte[] bytes1 = new byte[byteBuf1.readableBytes()];
////        byteBuf1.readBytes(bytes1);
////        bytesList.add(bytes1);
////    }
//
//    public static void main(String[] args) throws IOException {
//        int port = 18888; // 选择一个端口号
//        ServerSocket serverSocket = new ServerSocket(port);
//        System.out.println("TCP模拟服务器启动，正在监听端口: " + port);
//
//        int[] clients = {111, 112, 113};
//
//        // 启动定时任务，每5秒发送一次消息
//        scheduler.scheduleAtFixedRate(() -> {
//            for (int client : clients) {
//                MessageHeader report = new MessageHeader();
//                report.setCommand((byte) 0xAA);
//                report.setHospNo((short) 1);
//
//                report.setDeviceNo((byte) client);
//
//                report.setRunningState((byte) 1);
//                report.setExciteType((byte) 11);
//                report.setElectricity((short) (random.nextInt(9) + 1));
//                report.setResistance((short) 1);
//                report.setBattery((short) 33);
//                report.setSignal((short) 1);
//                report.setResidueTime((byte) 11);
//                report.setExtra(0);
//                report.setVerify((byte) 11);
//
//                // 将DeviceReport对象转换为字节数组
//                ByteBuf byteBuf = Unpooled.buffer(20);
//                byteBuf.writeByte(report.getCommand());
//                byteBuf.writeShort(report.getHospNo());
//                byteBuf.writeByte(report.getDeviceNo());
//                byteBuf.writeByte(report.getRunningState());
//                byteBuf.writeByte(report.getExciteType());
//                byteBuf.writeShort(report.getElectricity());
//                byteBuf.writeShort(report.getResistance());
//                byteBuf.writeShort(report.getBattery());
//                byteBuf.writeShort(report.getSignal());
//                byteBuf.writeByte(report.getResidueTime());
//                byteBuf.writeInt(report.getExtra());
//                byteBuf.writeByte(report.getVerify());
//
//                // 打印生成的字节数组
//                byte[] bytes = new byte[byteBuf.readableBytes()];
//                byteBuf.readBytes(bytes);
//
//                pushMessageToAll(bytes);
//            }
//        }, 0, 1, TimeUnit.SECONDS);
//
//        // 监听并接受连接
//        Socket clientSocket = serverSocket.accept();
//        System.out.println("TCP模拟服务器连接来自: " + clientSocket.getInetAddress().getHostAddress());
//
//        // 添加客户端到列表
//        connectedClients.add(clientSocket);
//
//        while (true) {
//            InputStream input = connectedClients.get(0).getInputStream();
//
//            byte[] buffer = new byte[1024]; // 缓冲区大小
//            int bytesRead;
//
//            while ((bytesRead = input.read(buffer)) != -1) { // 读取客户端数据
//                byte[] data = Arrays.copyOfRange(buffer, 0, bytesRead); // 复制实际读取的字节数
//                System.out.println("Received bytes: " + data.length);
//                System.out.println("Received data:" + data);
//
//                MessagePack messagePack = parseMessagePack(data);
//                System.out.println("TCP模拟服务器收到信息: " + messagePack);
//            }
//        }
//    }
//
//    private static MessagePack parseMessagePack(byte[] bytes) {
//        MessagePack messagePack = new MessagePack();
//        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(bytes));
//
//        try {
//            messagePack.setCommand(dataIn.readByte());
//            messagePack.setDeviceNo(dataIn.readByte());
//            messagePack.setRunCommand(dataIn.readByte());
//            messagePack.setExciteType(dataIn.readByte());
//            messagePack.setElectricityOut(dataIn.readShort());
//            messagePack.setExtra(new byte[2]);
//            dataIn.readFully(messagePack.getExtra());
//            messagePack.setVerify(dataIn.readByte());
//        } catch (IOException e) {
//            // 处理异常
//            e.printStackTrace();
//        }
//
//        return messagePack;
//    }
//
//
//    private static void pushMessageToAll(byte[] message) {
//
//        if (Objects.nonNull(message)) {
//            for (Socket client : connectedClients) {
//                try {
//                    OutputStream out = client.getOutputStream();
//                    out.write(message);
//                    out.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    // 如果发生异常，移除该客户端
//                    connectedClients.remove(client);
//                    try {
//                        client.close();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//}
