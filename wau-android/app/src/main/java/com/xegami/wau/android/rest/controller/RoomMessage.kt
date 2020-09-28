package com.xegami.wau.android.rest.controller

import com.google.gson.Gson
import com.xegami.wau.android.rest.dto.PlayerDTO

data class RoomMessage(val agent: PlayerDTO, val code: Int = -1, val password: String = "", val target: PlayerDTO? = null, val afk: Boolean = false, val rejoining: Boolean = false, val kicked: Boolean = false, val hotMode: Boolean = false, val roomToken: String? = null, val roundNumber: Int = -1) {

    fun toJson(): String {
        return Gson().toJson(this)
    }
}