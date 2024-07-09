package com.mini.tcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhl
 * @create 2024/7/8 9:56
 */
public class SimpleTCPServer {

    private static final List<Socket> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static Random random = new Random();
    private static ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) throws IOException {
        int port = 18888; // 选择一个端口号
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("TCP模拟服务器启动，正在监听端口: " + port);

        // 启动定时任务，每5秒发送一次消息
        scheduler.scheduleAtFixedRate(() -> {
            Map<String, Integer> map = new HashMap<String, Integer>() {{
                put("123", random.nextInt(99999) + 1);
                put("456", random.nextInt(99999) + 1);
                put("678", random.nextInt(99999) + 1);
                put("111", random.nextInt(99999) + 1);
            }};

            try {
                pushMessageToAll(objectMapper.writeValueAsString(map));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);

        while (true) {
            // 监听并接受连接
            Socket clientSocket = serverSocket.accept();
            System.out.println("TCP模拟服务器连接来自: " + clientSocket.getInetAddress().getHostAddress());

            // 添加客户端到列表
            connectedClients.add(clientSocket);

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
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
