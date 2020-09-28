package com.xegami.wau.api.dto;

import lombok.Data;

@Data
public class VotedPlayerPairDTO {

    private PlayerDTO voted;
    private PlayerDTO voting;

}
