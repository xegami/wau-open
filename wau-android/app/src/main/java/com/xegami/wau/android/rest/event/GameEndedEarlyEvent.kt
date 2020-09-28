package com.xegami.wau.android.rest.event

import com.xegami.wau.android.rest.dto.PlayerDTO

data class GameEndedEarlyEvent(val agent: PlayerDTO, val kicked: Boolean) : BaseEvent()