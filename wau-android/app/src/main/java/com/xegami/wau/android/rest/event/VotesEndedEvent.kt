package com.xegami.wau.android.rest.event

import com.xegami.wau.android.rest.dto.PlayerDTO

data class VotesEndedEvent(val agent: PlayerDTO) : BaseEvent()