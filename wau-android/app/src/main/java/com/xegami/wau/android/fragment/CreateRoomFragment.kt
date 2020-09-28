package com.xegami.wau.android.fragment

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.event.CreateRoomEvent
import com.xegami.wau.android.util.MyAnimationUtils
import kotlinx.android.synthetic.main.fragment_create_room.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CreateRoomFragment : Fragment() {

    private lateinit var fContext: Context
    private lateinit var mal: MainActivityListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_room, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        tv_fragment_create_room_hot_mode_label_2.paint.shader = LinearGradient(
            0f, 0f, 0f, tv_fragment_create_room_hot_mode_label_2.lineHeight.toFloat(),
            ContextCompat.getColor(fContext, R.color.hot_start), ContextCompat.getColor(fContext, R.color.hot_end), Shader.TileMode.REPEAT
        )

        et_fragment_create_room_nickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    when {
                        s.isNotEmpty() -> tv_fragment_create_room_nickname_hint.visibility = View.GONE
                        s.isEmpty() -> tv_fragment_create_room_nickname_hint.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        sw_fragment_create_room_hot_mode.setOnCheckedChangeListener { _, b ->
            if (b) {
                btn_fragment_create_room_create.isEnabled = false
                Handler().postDelayed({
                    mal.showBasicDialogFragment(getString(R.string.hot_mode_alert_body), null, false)
                    btn_fragment_create_room_create.isEnabled = true
                }, 250)
            }
        }

        cl_fragment_create_room_hot_mode.setOnClickListener {
            mal.showBasicDialogFragment(getString(R.string.hot_mode_alert_body), null, false)
        }

        btn_fragment_create_room_create.setOnClickListener { onCreateRoomButtonClick() }

        MyAnimationUtils.slideFDTU(btn_fragment_create_room_create)
        MyAnimationUtils.slideFUTD(tv_fragment_create_room_title)
        MyAnimationUtils.slideFUTD(cl_fragment_create_room_form)
    }

    private fun onCreateRoomButtonClick() {
        mal.playSound("button_click")

        val nickname = et_fragment_create_room_nickname.text.toString().trim()
        val hotMode = sw_fragment_create_room_hot_mode.isChecked

        if (nickname.isEmpty()) {
            mal.snack("Escribe un apodo para identificarte.")
            return
        }

        val playerDTO = PlayerDTO(
            nickname.toLowerCase().capitalize()
        )
        RoomController.instance.create(playerDTO, hotMode)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(createRoomEvent: CreateRoomEvent) {
        mal.gotoLobbyFragment()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}
