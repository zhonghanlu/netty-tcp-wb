package com.mini.codec.proto;

import lombok.Data;

/**
 * @author zhl
 */
@Data
public class Message {

    private MessageHeader messageHeader;

    private MessagePack messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }

    public static void main(String[] args) {
        String source = "6F AA 0D 05 6F 01 00 FE 0D 13 F9 64 E7 3A 7B 00 00 00 00 43";


        String[] hex = source.split(" ");//将接收的字符串按空格分割成数组
        byte[] byteArray = new byte[hex.length];

//        for (int i = 0; i < hex.length; i++) {
//            //parseInt()方法用于将字符串参数作为有符号的n进制整数进行解析
//            byteArray[i] = (byte) Integer.parseInt(hex[i], 16);
//        }

        System.out.println(byteArray);

        System.out.println(byteArray.length);

        byte lastByte = byteArray[byteArray.length - 1];
        System.out.println(lastByte);
//        String res = String.format("%02x", new Integer(lastByte & 0xff)).toUpperCase();
//
//        System.out.println("最后一位" + Integer.parseInt(res, 16));

        int sum = 0;
        for (int i = 0; i < byteArray.length - 1; i++) {
            sum += (byteArray[i] & 1);
        }

        System.out.println(sum);

    }
}
