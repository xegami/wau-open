package com.xegami.wau.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class VotedPlayerDTO implements Serializable {

    private PlayerDTO voted;
    private List<PlayerDTO> voting = new ArrayList<>();

    public VotedPlayerDTO(PlayerDTO voted, PlayerDTO voting) {
        this.voted = voted;
        this.voting.add(voting);
    }

    public void addVoting(PlayerDTO voting) {
        this.voting.add(voting);
    }

    public Integer getVoteCount() {
        return voting.size();
    }
}
