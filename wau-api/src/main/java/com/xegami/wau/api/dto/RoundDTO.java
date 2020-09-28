package com.xegami.wau.api.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoundDTO implements Serializable {

    private QuestionDTO question;
    @Setter(value = AccessLevel.PRIVATE)
    private List<VotedPlayerDTO> votedPlayers = new ArrayList<>();

    public void addVote(PlayerDTO voted, PlayerDTO voting) {
        VotedPlayerDTO votedPlayerDTO = votedPlayers.stream().filter(v -> v.getVoted().getNickname().equals(voted.getNickname())).findAny().orElse(null);

        if (votedPlayerDTO != null) {
            votedPlayers.get(votedPlayers.indexOf(votedPlayerDTO)).addVoting(voting);
        } else {
            votedPlayers.add(new VotedPlayerDTO(voted, voting));
        }
    }

}
