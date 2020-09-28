package com.xegami.wau.api.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoomDTO implements Serializable {

    @Setter(value = AccessLevel.PRIVATE)
    private List<PlayerDTO> players = new ArrayList<>();
    private Integer code;
    private String password;
    private List<RoundDTO> rounds = new ArrayList<>();
    private List<PlayerDTO> votingNextPlayers = new ArrayList<>();
    private List<PlayerDTO> votingSkipPlayers = new ArrayList<>();
    private List<PlayerDTO> votingCancelSkipPlayers = new ArrayList<>();
    private List<PlayerDTO> afkPlayers = new ArrayList<>();
    private PlayerDTO partyLeader;
    private Integer roundNumber = 0;
    private String screen = "lobby";
    private boolean hotMode;
    private Long createDate;
    private String token;

    public void increaseRound() {
        roundNumber++;
    }

    public void clearVotingPlayers() {
        votingNextPlayers.clear();
        votingSkipPlayers.clear();
        votingCancelSkipPlayers.clear();
    }

}
