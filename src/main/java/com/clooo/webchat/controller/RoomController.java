package com.clooo.webchat.controller;

import com.clooo.webchat.ws.manager.RoomSessionManager;
import com.clooo.webchat.ws.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomSessionManager roomSessionManager;

    public RoomController(RoomSessionManager roomSessionManager) {
        this.roomSessionManager = roomSessionManager;
    }

    @GetMapping("/create/{id}")
    public Result createRoom(@PathVariable Integer id) {
        String roomCode = roomSessionManager.createRoom(id);
        return Result.ok(roomCode);
    }

    @GetMapping("/join/{roomCode}/{id}")
    public Result joinRoom(@PathVariable String roomCode, @PathVariable Integer id) {
        Boolean joinRoom = roomSessionManager.joinRoom(roomCode, id);
        if (joinRoom) {
            return Result.ok();
        }
        return Result.error("enter " + roomCode + " failure");
    }

    @GetMapping("/left/{roomCode}/{id}")
    public Result leftRoom(@PathVariable String roomCode, @PathVariable Integer id) {
        Boolean left = roomSessionManager.leftRoom(roomCode, id);
        return Result.ok(left);
    }

    @GetMapping("/members/{roomCode}")
    public Result members(@PathVariable String roomCode) {
        List<Integer> roomMembers = roomSessionManager.getRoomMembers(roomCode);
        return Result.ok(roomMembers);
    }

}

