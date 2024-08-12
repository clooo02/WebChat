package com.clooo.webchat.ws.handler;


import com.clooo.webchat.ws.message.TransferMessage;
import com.clooo.webchat.ws.message.type.TransferDataType;
import com.clooo.webchat.ws.message.type.TransferMessageType;
import com.clooo.webchat.ws.protocol.TransferProtocol;
import com.clooo.webchat.ws.utils.FileUploadContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FileMessageHandler {

    private final Map<ChannelId, FileUploadContext> uploadContexts = new ConcurrentHashMap<>();

    public void handleFileMessage(ChannelHandlerContext ctx, TransferMessage message) throws IOException {
        switch (message.getType()) {
            case TransferDataType.FILE_REQUEST -> startFileDownload(ctx, message);
            case TransferDataType.FILE_START -> startFileUpload(ctx, message);
            case TransferDataType.FILE_CHUNK -> saveChunk(ctx, message.getData());
            case TransferDataType.FILE_END -> closeFile(ctx, message);
            default -> throw new IllegalArgumentException("Unknown message type: " + message.getType());
        }
    }

    private void startFileUpload(ChannelHandlerContext ctx, TransferMessage message) throws IOException {
        String fileName = new String(message.getData(), StandardCharsets.UTF_8);
        System.out.println(fileName + "上传开始");
        FileUploadContext uploadContext = new FileUploadContext(fileName);
        uploadContexts.put(ctx.channel().id(), uploadContext);
    }

    private void saveChunk(ChannelHandlerContext ctx, byte[] data) throws IOException {
        FileUploadContext uploadContext = uploadContexts.get(ctx.channel().id());
        if (uploadContext != null) {
            uploadContext.getFos().write(data);
        }
    }

    private void closeFile(ChannelHandlerContext ctx, TransferMessage message) throws IOException {
        String fileName = new String(message.getData(), StandardCharsets.UTF_8);
        System.out.println(fileName + "上传结束");
        FileUploadContext uploadContext = uploadContexts.remove(ctx.channel().id());
        if (uploadContext != null) {
            uploadContext.close();
        }
    }

    private void startFileDownload(ChannelHandlerContext ctx, TransferMessage message) throws IOException {
        String fileName = new String(message.getData(), StandardCharsets.UTF_8);
        System.out.println(ctx.channel().id() + "准备下载文件：" + fileName);
        File file = new File(fileName);

        if (!file.exists()) {
            System.err.println("Requested file not found: " + fileName);
//            ctx.close();
            return;
        }

        ByteBuf buffer = ctx.alloc().buffer();
        TransferMessage startMessage = new TransferMessage(
                TransferMessageType.SYSTEM_RESPONSE, message.getMessageId(),
                TransferMessage.SYSTEM_ID, message.getFromId(),
                message.getDataType(), fileName.getBytes(StandardCharsets.UTF_8)
        );
        TransferProtocol.serializeMessage(startMessage, buffer);
        ctx.writeAndFlush(new BinaryWebSocketFrame(buffer));

        try (InputStream is = new FileInputStream(file)) {
            byte[] chunk = new byte[TransferProtocol.MAX_CONTENT_SIZE - TransferProtocol.HEADER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(chunk)) != -1) {
                ByteBuf chunkBuffer = Unpooled.wrappedBuffer(chunk, 0, bytesRead);
                TransferMessage chunkMessage = new TransferMessage(
                        TransferMessageType.SYSTEM_RESPONSE, message.getMessageId(),
                        TransferMessage.SYSTEM_ID, message.getFromId(),
                        message.getDataType(), chunkBuffer.array());
                ByteBuf outBuffer = ctx.alloc().buffer();
                TransferProtocol.serializeMessage(chunkMessage, outBuffer);
                ctx.writeAndFlush(new BinaryWebSocketFrame(outBuffer));
            }
        }

        ByteBuf endBuffer = ctx.alloc().buffer();
        TransferMessage endMessage = new TransferMessage(
                TransferMessageType.SYSTEM_RESPONSE, message.getMessageId(),
                TransferMessage.SYSTEM_ID, message.getFromId(),
                message.getDataType(), new byte[0]);
        TransferProtocol.serializeMessage(endMessage, endBuffer);
        ctx.writeAndFlush(new BinaryWebSocketFrame(endBuffer)).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("已成功发送" + fileName);
            } else {
                System.out.println("发送失败：" + fileName);
            }
        });
    }


}
