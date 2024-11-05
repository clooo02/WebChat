package com.clooo.webchat;

import com.clooo.webchat.ws.message.TransferMessage;
import com.clooo.webchat.ws.message.type.MessageType;
import com.clooo.webchat.ws.protocol.TransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public class BufferTests {
    @Test
    void byteBuffer2byteBuf() {
        byte[] data = "hello".getBytes();
        ByteBuffer byteBuffer = TransferProtocol.serializeMessage(
                new TransferMessage(
                        MessageType.SYSTEM_REQUEST, 1,
                        1, 1,
                        data
                ), ByteBuffer.allocate(TransferProtocol.HEADER_SIZE + data.length));
        /*
         * 当向 ByteBuffer 中写入数据时，position 指针会不断移动，指向下一个可以写入数据的位置。
         * 在读取数据前，你必须调用 flip() 方法，它会将 position 设置为 0，并将 limit 设置为当前的 position，让缓冲区切换到读模式。
         * 如果不调用 flip()，position 还在最后一个写入的位置，当你调用 get() 读取数据时，
         * 会从不正确的位置开始读取，从而导致 BufferUnderflowException（缓冲区没有足够的数据可读取）。
         */
        byteBuffer.flip();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
        TransferMessage message = TransferProtocol.deserializeMessage(byteBuf);
        System.out.println(message);
    }

    @Test
    void byteBuf2byteBuffer() {
        byte[] data = "hello, world!".getBytes();
        ByteBuf byteBuf = Unpooled.buffer(TransferProtocol.HEADER_SIZE + data.length);

        TransferProtocol.serializeMessage(new TransferMessage(MessageType.SYSTEM_REQUEST, 1,
                1, 1, data), byteBuf);
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        /*
         * ByteBuf 和 ByteBuffer 的概念不完全一致。
         * Netty 的 ByteBuf 本质上更灵活，它的 nioBuffer() 方法直接返回一个 ByteBuffer，
         * 该 ByteBuffer 共享 ByteBuf 的内存，并已经设置好了读取模式。
         * 也就是说，nioBuffer() 返回的 ByteBuffer 已经处于可以被读取的状态。
         * 不需要调用 flip()：因为 ByteBuf 和 ByteBuffer 共享了相同的数据，
         * 当 nioBuffer() 返回时，ByteBuffer 已经准备好被读取。
         * 如果你再调用 flip()，会导致 position 被重置为 0，
         * 从而导致 BufferUnderflowException，因为此时 position 已经不正确，读取的数据量不足。
         */
        TransferMessage message = TransferProtocol.deserializeMessage(byteBuffer);
        System.out.println(message);
    }
}
