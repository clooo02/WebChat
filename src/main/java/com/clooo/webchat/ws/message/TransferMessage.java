package com.clooo.webchat.ws.message;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class TransferMessage implements Serializable {

    public static final int SYSTEM_ID = 0;
    @Serial
    private static final long serialVersionUID = 1L;

    private byte type;
    private int messageId;

    private int fromId;

    private int toId;

    private byte dataType;

    private byte[] data;

    public TransferMessage(byte type, int messageId,
                           int fromId, int toId,
                           byte dataType, byte[] data) {
        this.type = type;
        this.messageId = messageId;
        this.fromId = fromId;
        this.toId = toId;
        this.dataType = dataType;
        this.data = data;
    }

    public byte getType() {
        return type;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public byte getDataType() {
        return dataType;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "TransferMessage{" +
                "type=" + type +
                ", messageId=" + messageId +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", dataType=" + dataType +
                ", data=" + new String(data, StandardCharsets.UTF_8) +
                '}';
    }
}
