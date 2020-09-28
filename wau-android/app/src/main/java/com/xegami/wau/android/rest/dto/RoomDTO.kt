package com.xegami.wau.android.rest.dto

import java.io.Serializable

data class RoomDTO(
    val players: List<PlayerDTO>,
    val code: Int,
    val password: String,
    val rounds: List<RoundDTO>,
    val votingNextPlayers: List<PlayerDTO>,
    val votingSkipPlayers: List<PlayerDTO>,
    val votingCancelSkipPlayers: List<PlayerDTO>,
    val afkPlayers: List<PlayerDTO>,
    var partyLeader: PlayerDTO,
    var roundNumber: Int,
    val screen: String,
    val hotMode: Boolean,
    val createDate: Long,
    val token: String
) : Serializable
