package com.clooo.webchat.ws.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChannelManager {

    private final Map<ChannelId, Channel> channelMap;
    private final Map<Integer, Channel> channelUserMap;
    private final Map<ChannelId, Integer> userIdMap;

    public ChannelManager() {
        channelMap = new ConcurrentHashMap<>();
        channelUserMap = new ConcurrentHashMap<>();
        userIdMap = new ConcurrentHashMap<>();
    }

    public void setChannelByChannelId(ChannelId channelId, Channel channel) {
        channelMap.put(channelId, channel);
    }

    public void setChannelByUserId(Integer userId, Channel channel) {
        channelUserMap.put(userId, channel);
    }

    public void setUserIdByChannelId(ChannelId channelId, Integer userId) {
        userIdMap.put(channelId, userId);
    }


    public Channel getChannelById(ChannelId channelId) {
        return channelMap.get(channelId);
    }

    public Channel getChannelByUserId(Integer userId) {
        return channelUserMap.get(userId);
    }

    public Integer getUserId(ChannelId channelId) {
        return userIdMap.get(channelId);
    }

    public Integer removeChannel(ChannelId channelId) {
        channelMap.remove(channelId);
        Integer userId = userIdMap.remove(channelId);
        if (userId != null) {
            channelUserMap.remove(userId);
        }
        return userId;
    }

    public Map<ChannelId, Channel> getChannelMap() {
        return channelMap;
    }

    public Map<Integer, Channel> getChannelUserMap() {
        return channelUserMap;
    }

    public Map<ChannelId, Integer> getUserIdMap() {
        return userIdMap;
    }
}
