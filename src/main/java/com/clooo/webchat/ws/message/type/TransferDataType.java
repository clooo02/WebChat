package com.clooo.webchat.ws.message.type;

public class TransferDataType {
    public static final byte DEFAULT = 0x00;
    public static final byte FILE_START = 0x01;
    public static final byte FILE_CHUNK = 0x02;
    public static final byte FILE_END = 0x03;
    public static final byte FILE_REQUEST = 0x04;
    public static final byte WEBRTC_SDP_OFFER = 0x05;
    public static final byte WEBRTC_SDP_ANSWER = 0x06;
    public static final byte WEBRTC_ICE_CANDIDATE = 0x07;
    public static final byte WEBRTC_ERROR = 0x08;

}
