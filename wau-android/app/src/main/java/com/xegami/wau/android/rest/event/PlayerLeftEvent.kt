package com.xegami.wau.android.rest.event

import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.dto.RoomDTO

data class PlayerLeftEvent(val agent: PlayerDTO, val kicked: Boolean) : BaseEvent()