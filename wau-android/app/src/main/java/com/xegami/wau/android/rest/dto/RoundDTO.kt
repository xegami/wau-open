package com.xegami.wau.android.rest.dto

import java.io.Serializable

data class RoundDTO(val question: QuestionDTO, var votedPlayers: List<VotedPlayerDTO>) : Serializable
