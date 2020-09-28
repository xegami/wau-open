package com.xegami.wau.android.rest.event

import com.xegami.wau.android.rest.dto.PlayerDTO

data class PlayerVotedEvent(val agent: PlayerDTO) : BaseEvent()