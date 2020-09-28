package com.xegami.wau.android.rest.dto

import java.io.Serializable

data class VotedPlayerPairDTO(val voted: PlayerDTO, val voting: PlayerDTO) : Serializable
