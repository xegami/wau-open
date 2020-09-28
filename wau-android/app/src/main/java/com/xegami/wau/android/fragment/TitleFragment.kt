package com.xegami.wau.android.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.event.ConnectionClosedEvent
import com.xegami.wau.android.rest.event.ConnectionOpenedEvent
import kotlinx.android.synthetic.main.fragment_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.NullPointerException
import java.lang.RuntimeException

class TitleFragment : Fragment() {

    private lateinit var fContext: Context
    private lateinit var mal: MainActivityListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_title, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        btn_fragment_title_play.setOnClickListener { onPlayButtonClick() }

        tv_fragment_title_subtitle.typeface = Typeface.createFromAsset(fContext.assets, "fonts/helvetica.otf")

        if (RoomController.instance.isConnected()) {
            btn_fragment_title_play.isEnabled = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(connectionOpenedEvent: ConnectionOpenedEvent) {
        btn_fragment_title_play.isEnabled = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(connectionClosedEvent: ConnectionClosedEvent) {
        btn_fragment_title_play.isEnabled = false
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    private fun onPlayButtonClick() {
        mal.playSound("button_click")

        mal.gotoMainMenuFragment(false)
    }

}
