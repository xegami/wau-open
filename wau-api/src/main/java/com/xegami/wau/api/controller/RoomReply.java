package com.xegami.wau.api.controller;

import com.xegami.wau.api.dto.PlayerDTO;
import com.xegami.wau.api.dto.RoomDTO;
import lombok.Data;

@Data
public class RoomReply {

    private RoomDTO room;
    private PlayerDTO agent;
    private boolean kicked;

    public RoomReply(RoomDTO room) {
        this.room = room;
    }

    public RoomReply(RoomDTO room, PlayerDTO agent) {
        this.room = room;
        this.agent = agent;
    }

    public RoomReply(RoomDTO room, PlayerDTO agent, boolean kicked) {
        this.room = room;
        this.agent = agent;
        this.kicked = kicked;
    }

}
