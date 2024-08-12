package com.clooo.webchat.ws.manager;

import com.clooo.webchat.ws.message.TransferMessage;
import com.clooo.webchat.ws.message.type.TransferDataType;
import com.clooo.webchat.ws.message.type.TransferMessageType;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SingleSessionManager {

    private final ChannelManager channelManager;

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private final Map<Integer, String> userSessionMap = new ConcurrentHashMap<>();

    public SingleSessionManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public void handlerMessage(TransferMessage message) {
        switch (message.getDataType()) {
            case TransferDataType.DEFAULT -> {
                handleDefaultMessage(message);
            }
            case TransferDataType.WEBRTC_SDP_OFFER,
                    TransferDataType.WEBRTC_SDP_ANSWER,
                    TransferDataType.WEBRTC_ICE_CANDIDATE -> {
                forwardMessageToPeer(message);
            }
            case TransferDataType.WEBRTC_ERROR -> {
                removeSession(message.getFromId());
            }
        }
    }


    private void handleDefaultMessage(TransferMessage message) {
        int fromId = message.getFromId();
        String code = new String(message.getData());
        Session session = sessionMap.get(code);

        if (session == null) {
            // 当前会话不存在，创建会话
            userSessionMap.put(fromId, code);
            Session s = new Session(fromId);
            sessionMap.put(code, s);
        } else {
            // 会话存在，用户加入会话
            if (session.isFull()) {
                // 该会话人数已满
                notifyPeer(fromId, TransferDataType.WEBRTC_ERROR, "加入房间失败".getBytes());
            } else {
                session.setU2(fromId);
                userSessionMap.put(fromId, code);
                // 通知用户1，用户2到来
                Integer u1 = session.getU1();
                notifyPeer(u1, TransferDataType.DEFAULT, ByteBuffer.allocate(4).putInt(fromId).array());
            }
        }
    }

    private void forwardMessageToPeer(TransferMessage message) {
        channelManager.getChannelByUserId(message.getToId()).writeAndFlush(message);
    }

    public void removeSession(Integer userId) {
        if (userSessionMap.containsKey(userId)) {
            String code = userSessionMap.remove(userId);
            if (code != null) {
                Session session = sessionMap.remove(code);
                Integer u1 = session.getU1();
                Integer u2 = session.getU2();
                if (u1 != null && !u1.equals(userId)) {
                    userSessionMap.remove(u1);
                    notifyPeer(u1, TransferDataType.WEBRTC_ERROR, "当前会话已结束".getBytes());
                }
                if (u2 != null && !u2.equals(userId)) {
                    userSessionMap.remove(u2);
                    notifyPeer(u2, TransferDataType.WEBRTC_ERROR, "当前会话已结束".getBytes());
                }
            }
        }
    }

    private void notifyPeer(Integer peerId, byte dataType, byte[] info) {
        if (peerId != null) {
            TransferMessage error = new TransferMessage(
                    TransferMessageType.WEBRTC, 0,
                    TransferMessage.SYSTEM_ID, peerId,
                    dataType, info
            );
            channelManager.getChannelByUserId(peerId).writeAndFlush(error);
        }
    }


    static class Session {

        private final Integer u1;
        private Integer u2;

        public Session(Integer u1) {
            this.u1 = u1;
        }

        public Integer getU1() {
            return u1;
        }


        public Integer getU2() {
            return u2;
        }

        public void setU2(Integer u2) {
            this.u2 = u2;
        }

        public boolean isFull() {
            return u2 != null;
        }
    }

}