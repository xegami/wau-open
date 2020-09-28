package com.xegami.wau.api.controller;

import com.xegami.wau.api.dto.PlayerDTO;
import com.xegami.wau.api.dto.RoomDTO;
import lombok.Data;

@Data
public class RoomMessage {

    private PlayerDTO agent;
    private Integer code;
    private String password;
    private PlayerDTO target;
    private boolean afk;
    private boolean rejoining;
    private boolean kicked;
    private boolean hotMode;
    private String roomToken;
    private Integer roundNumber;

}
