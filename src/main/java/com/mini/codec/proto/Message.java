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
}
