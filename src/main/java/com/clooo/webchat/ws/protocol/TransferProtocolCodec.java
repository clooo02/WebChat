package com.clooo.webchat.ws.protocol;

import com.clooo.webchat.ws.message.TransferMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

@ChannelHandler.Sharable
public class TransferProtocolCodec extends MessageToMessageCodec<BinaryWebSocketFrame, TransferMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TransferMessage message, List<Object> list) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        TransferProtocol.serializeMessage(message, buffer);
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
        list.add(frame);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame binaryWebSocketFrame, List<Object> list) throws Exception {
        ByteBuf content = binaryWebSocketFrame.content();
        TransferMessage message = TransferProtocol.deserializeMessage(content);
        list.add(message);
    }

}
