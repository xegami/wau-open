package com.xegami.wau.android.rest.event

import org.greenrobot.eventbus.EventBus

open class BaseEvent {

    fun post() {
        EventBus.getDefault().post(this)
    }
}