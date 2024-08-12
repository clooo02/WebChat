package com.clooo.webchat.ws.message.type;

public class TransferMessageType {
    @Deprecated
    public static final byte CREATE_ROOM = 0x01;
    @Deprecated
    public static final byte CREATE_ROOM_RESPONSE = 0x02;
    @Deprecated
    public static final byte JOIN_ROOM = 0x03;
    @Deprecated
    public static final byte JOIN_ROOM_RESPONSE = 0x04;
    public static final byte ROOM_USER_JOIN = 0x05;
    public static final byte ROOM_USER_QUIT = 0x06;
    public static final byte ROOM_MESSAGE = 0x07;
    public static final byte SYSTEM_REQUEST = 0x08;
    public static final byte SYSTEM_RESPONSE= 0x09;
    public static final byte WEBRTC= 0x0a;
    public static final byte LOGIN= 0x0b;
}
