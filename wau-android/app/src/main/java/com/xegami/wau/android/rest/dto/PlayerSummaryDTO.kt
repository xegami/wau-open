package com.xegami.wau.android.rest.dto

import java.io.Serializable

data class PlayerSummaryDTO(val index: Int, val nickname: String, val comments: MutableList<String> = mutableListOf()) : Serializable
