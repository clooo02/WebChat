package com.clooo.webchat.ws.handler;


import com.clooo.webchat.ws.manager.ChannelManager;
import com.clooo.webchat.ws.manager.RoomSessionManager;
import com.clooo.webchat.ws.manager.SingleSessionManager;
import com.clooo.webchat.ws.message.TransferMessage;
import com.clooo.webchat.ws.message.type.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.List;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<TransferMessage> {

    private final ChannelManager channelManager;

    private final RoomSessionManager roomSessionManager;
    private final SingleSessionManager singleSessionManager;

    public WebSocketServerHandler(RoomSessionManager roomSessionManager, ChannelManager channelManager,
                                  SingleSessionManager singleSessionManager) {
        this.roomSessionManager = roomSessionManager;
        this.channelManager = channelManager;
        this.singleSessionManager = singleSessionManager;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channelManager.setChannelByChannelId(ctx.channel().id(), ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws IOException {
        closeSession(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeSession(ctx);
    }

    private void closeSession(ChannelHandlerContext ctx) {
        Integer userId = channelManager.removeChannel(ctx.channel().id());
        // 根据userId断开相关连接
        if (userId != null) {
            roomSessionManager.quitAllRoom(userId);
            singleSessionManager.removeSession(userId);
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransferMessage message) throws IOException {

        handleWebSocketFrame(ctx, message);

    }


    private void handleWebSocketFrame(ChannelHandlerContext ctx, TransferMessage message) throws IOException {
        byte messageType = message.getType();
        int fromId = message.getFromId();
        final Channel channel = ctx.channel();
        switch (messageType) {
            case MessageType.LOGIN -> {
                channelManager.setChannelByUserId(fromId, channel);
                channelManager.setUserIdByChannelId(channel.id(), fromId);
                TransferMessage o = new TransferMessage(MessageType.SYSTEM_RESPONSE, 0,
                        TransferMessage.SYSTEM_ID, fromId,
                        "Login Success".getBytes());
                ctx.channel().writeAndFlush(o);
            }
            case MessageType.ROOM_MESSAGE -> {
                List<Integer> roomMembers = roomSessionManager.getRoomMembers(String.valueOf(message.getToId()));
                for (Integer userId : roomMembers) {
                    if (userId != fromId) {
                        Channel c = channelManager.getChannelByUserId(userId);
                        c.writeAndFlush(message);
                    }
                }
            }
            case MessageType.WEBRTC_INFO, MessageType.WEBRTC_ERROR,
                    MessageType.WEBRTC_SDP_ANSWER, MessageType.WEBRTC_SDP_OFFER,
                    MessageType.WEBRTC_ICE_CANDIDATE -> {
                singleSessionManager.handlerMessage(message);
            }
            default -> throw new IllegalArgumentException("Unknown message type: " + message.getType());
        }

    }


}
