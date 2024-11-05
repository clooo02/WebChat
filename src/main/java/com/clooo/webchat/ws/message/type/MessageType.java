package com.clooo.webchat.ws.message.type;

public class MessageType {
    public static final byte ROOM_USER_JOIN = 0x01;
    public static final byte ROOM_USER_QUIT = 0x02;
    public static final byte ROOM_MESSAGE = 0x03;
    public static final byte SYSTEM_REQUEST = 0x04;
    public static final byte SYSTEM_RESPONSE= 0x05;
    public static final byte WEBRTC_SDP_OFFER = 0x06;
    public static final byte WEBRTC_SDP_ANSWER = 0x07;
    public static final byte WEBRTC_ICE_CANDIDATE = 0x08;
    public static final byte WEBRTC_ERROR = 0x09;
    public static final byte WEBRTC_INFO = 0x0a;
    public static final byte LOGIN= 0x0b;

}
