package com.clooo.webchat.ws.manager;

import com.clooo.webchat.ws.message.TransferMessage;
import com.clooo.webchat.ws.message.type.TransferDataType;
import com.clooo.webchat.ws.message.type.TransferMessageType;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class RoomSessionManager {
    private static final int CODE_LENGTH = 6;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final ChannelManager channelManager;

    public RoomSessionManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public String createRoom(Integer ownerId) {
        String roomCode;
        do {
            roomCode = generateRoomCode();
        } while (rooms.containsKey(roomCode));
        rooms.put(roomCode, new Room(ownerId));
        log.info(ownerId + "成功创建房间：" + roomCode);
        return roomCode;
    }

    public Boolean joinRoom(String roomCode, Integer fromId) {
        Room room = rooms.get(roomCode);

        if (room != null && room.getUsers().size() <= 10 && !room.getUsers().contains(fromId)) {
            room.addUser(fromId);
            TransferMessage message = new TransferMessage(
                    TransferMessageType.ROOM_USER_JOIN, 0,
                    TransferMessage.SYSTEM_ID, Integer.parseInt(roomCode),
                    TransferDataType.DEFAULT, ByteBuffer.allocate(4).putInt(fromId).array()
            );
            // TODO 通过MQ向房间内用户提醒新用户到达
            for (Integer user : room.getUsers()) {
                if (!user.equals(fromId)) {
                    Channel channel = channelManager.getChannelByUserId(user);
                    if (channel != null) {
                        channel.writeAndFlush(message);
                    }
                }
            }
            return true;
        } else {
            return false;
        }

    }


    public Boolean leftRoom(String roomCode, Integer fromId) {
        Boolean removed = false;
        Room room = rooms.get(roomCode);
        if (room != null && room.getUsers().contains(fromId)) {
            removed = room.removeUser(fromId);
            if (room.getUsers().size() == 0) {
                rooms.remove(roomCode);
            } else {
                // 发送一条消息通知房间内的有用户退出房间
                TransferMessage message = new TransferMessage(
                        TransferMessageType.ROOM_USER_QUIT, 0,
                        TransferMessage.SYSTEM_ID, Integer.parseInt(roomCode),
                        TransferDataType.DEFAULT, ByteBuffer.allocate(4).putInt(fromId).array()
                );
                for (Integer user : room.getUsers()) {
                    if (!user.equals(fromId)) {
                        Channel channel = channelManager.getChannelByUserId(user);
                        if (channel != null) {
                            channel.writeAndFlush(message);
                        }
                    }
                }
            }
        } else removed = true;
        return removed;
    }

    public void quitAllRoom(Integer fromId) {
        for (String roomCode : rooms.keySet()) {
            leftRoom(roomCode, fromId);
        }
    }

    public List<Integer> getRoomMembers(String roomCode) {
        Room room = rooms.get(roomCode);
        return room.getUsers();
    }


    private String generateRoomCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    static class Room {
        private final Integer ownerId;
        private final List<Integer> users = new ArrayList<>();

        public Room(Integer ownerId) {
            this.ownerId = ownerId;
            addUser(ownerId);
        }

        public void addUser(Integer id) {
            users.add(id);
        }

        public Boolean removeUser(Integer id) {
            return users.remove(id);
        }

        public List<Integer> getUsers() {
            return users;
        }

        @Override
        public String toString() {
            return "Room{" +
                    "ownerId='" + ownerId + '\'' +
                    ", users=" + users +
                    '}';
        }
    }
}


