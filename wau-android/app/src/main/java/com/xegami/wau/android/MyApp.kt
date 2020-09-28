package com.xegami.wau.android

import android.app.Application
import android.graphics.Typeface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.xegami.wau.android.listener.AppVisibilityChangeListener

class MyApp : Application(), LifecycleObserver {

    companion object {
        lateinit var instance: MyApp
        lateinit var appTypeface: Typeface
    }

    private var appVisibilityChangeListener: AppVisibilityChangeListener? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        isAppInBackground(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        isAppInBackground(true)
    }

    private fun isAppInBackground(isInBackground: Boolean) {
        if (appVisibilityChangeListener != null) {
            appVisibilityChangeListener!!.onChange(isInBackground)
        }
    }

    fun setOnAppVisibilityChangeListener(appVisibilityChangeListener: AppVisibilityChangeListener) {
        this.appVisibilityChangeListener = appVisibilityChangeListener
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        appTypeface = Typeface.createFromAsset(applicationContext.assets, "fonts/helvetica.otf")

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

}