package com.clooo.webchat.ws.protocol;

import com.clooo.webchat.ws.message.TransferMessage;
import io.netty.buffer.ByteBuf;


public class TransferProtocol {
    public static final int MAX_CONTENT_SIZE = 1024 * 64;

    // 消息头部大小
    public static final int HEADER_SIZE = 18;


    public static TransferMessage deserializeMessage(ByteBuf byteBuf) {
        byte messageType = byteBuf.readByte();
        int messageId = byteBuf.readInt();
        int fromId = byteBuf.readInt();
        int toId = byteBuf.readInt();
        byte dataType = byteBuf.readByte();
        int dataLength = byteBuf.readInt();

        byte[] dataBytes = new byte[dataLength];
        byteBuf.readBytes(dataBytes);

        return new TransferMessage(
                messageType, messageId,
                fromId, toId,
                dataType, dataBytes
        );
    }

    public static void serializeMessage(TransferMessage message, ByteBuf buffer) {
        // 1字节的消息类型
        buffer.writeByte(message.getType());
        // 4字节的消息ID
        buffer.writeInt(message.getMessageId());
        buffer.writeInt(message.getFromId());
        buffer.writeInt(message.getToId());
        buffer.writeByte(message.getDataType());
        // 4字节的消息长度
        buffer.writeInt(message.getData().length);
        buffer.writeBytes(message.getData());
    }
}
