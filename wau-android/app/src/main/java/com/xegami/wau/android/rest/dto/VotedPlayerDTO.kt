package com.xegami.wau.android.rest.dto

import java.io.Serializable

data class VotedPlayerDTO(val voted: PlayerDTO, val voting: List<PlayerDTO>) : Serializable
