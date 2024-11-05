package com.clooo.webchat.ws.protocol;

import com.clooo.webchat.ws.message.TransferMessage;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;


public class TransferProtocol {
    public static final int MAX_CONTENT_SIZE = 1024 * 64;

    // 消息头部大小
    public static final int HEADER_SIZE = 17;


    public static TransferMessage deserializeMessage(ByteBuf byteBuf) {
        byte messageType = byteBuf.readByte();
        int messageId = byteBuf.readInt();
        int fromId = byteBuf.readInt();
        int toId = byteBuf.readInt();
        int dataLength = byteBuf.readInt();
        byte[] dataBytes = new byte[dataLength];
        byteBuf.readBytes(dataBytes);
        return new TransferMessage(
                messageType, messageId,
                fromId, toId,
                dataBytes
        );
    }

    public static void serializeMessage(TransferMessage message, ByteBuf buffer) {
        // 1字节的消息类型
        buffer.writeByte(message.getType());
        // 4字节的消息ID
        buffer.writeInt(message.getMessageId());
        buffer.writeInt(message.getFromId());
        buffer.writeInt(message.getToId());
        // 4字节的消息长度
        buffer.writeInt(message.getData().length);
        buffer.writeBytes(message.getData());
    }


    public static TransferMessage deserializeMessage(ByteBuffer byteBuffer) {
        // 读取消息类型 (1 byte)
        byte messageType = byteBuffer.get();

        // 读取消息ID (4 bytes)
        int messageId = byteBuffer.getInt();

        // 读取fromId (4 bytes)
        int fromId = byteBuffer.getInt();

        // 读取toId (4 bytes)
        int toId = byteBuffer.getInt();

        // 读取数据长度 (4 bytes)
        int dataLength = byteBuffer.getInt();

        // 读取数据内容
        byte[] dataBytes = new byte[dataLength];
        byteBuffer.get(dataBytes);  // 读取dataBytes长度的字节

        // 返回反序列化后的消息对象
        return new TransferMessage(
                messageType, messageId,
                fromId, toId,
                dataBytes
        );
    }

    public static ByteBuffer serializeMessage(TransferMessage message, ByteBuffer buffer) {
        // 1字节的消息类型
        buffer.put(message.getType());

        // 4字节的消息ID
        buffer.putInt(message.getMessageId());

        // 4字节的fromId
        buffer.putInt(message.getFromId());

        // 4字节的toId
        buffer.putInt(message.getToId());

        // 4字节的数据长度
        buffer.putInt(message.getData().length);

        // 写入数据
        buffer.put(message.getData());
        return buffer;
    }

}
