package com.xegami.wau.android.rest.controller

import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.dto.RoomDTO

data class RoomReply(val room: RoomDTO, val agent: PlayerDTO, val kicked: Boolean)