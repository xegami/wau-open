package com.xegami.wau.android.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.event.JoinRoomEvent
import com.xegami.wau.android.util.MyAnimationUtils
import com.xegami.wau.android.util.MyMiscUtils
import kotlinx.android.synthetic.main.fragment_join_room.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class JoinRoomFragment : Fragment() {

    private lateinit var fContext: Context
    private lateinit var mal: MainActivityListener
    private var invitingNickname: String? = null
    private var roomToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_join_room, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        if (invitingNickname != null && roomToken != null) {
            tv_fragment_join_room_title.text = "Unirse a la sala de $invitingNickname"
            et_fragment_join_room_code.visibility = View.GONE
            tv_fragment_join_room_code_hint.visibility = View.GONE
            et_fragment_join_room_password.visibility = View.GONE
            tv_fragment_join_room_password_hint.visibility = View.GONE
        }

        btn_fragment_join_room_join.setOnClickListener { onJoinRoomButtonClick() }

        et_fragment_join_room_nickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    when {
                        s.isNotEmpty() -> tv_fragment_join_room_nickname_hint.visibility = View.GONE
                        s.isEmpty() -> tv_fragment_join_room_nickname_hint.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        et_fragment_join_room_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    when {
                        s.isNotEmpty() -> tv_fragment_join_room_code_hint.visibility = View.GONE
                        s.isEmpty() -> tv_fragment_join_room_code_hint.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        et_fragment_join_room_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    when {
                        s.length == 4 -> {
                            MyMiscUtils.hideSoftKeyboard(fContext, view)
                            //et_fragment_join_room_password.clearFocus()
                        }
                        s.isNotEmpty() -> tv_fragment_join_room_password_hint.visibility = View.GONE
                        s.isEmpty() -> tv_fragment_join_room_password_hint.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        MyAnimationUtils.slideFUTD(tv_fragment_join_room_title)
        MyAnimationUtils.slideFUTD(cl_fragment_join_room_form)
        MyAnimationUtils.slideFDTU(btn_fragment_join_room_join)
    }

    private fun onJoinRoomButtonClick() {
        mal.playSound("button_click")

        val nickname = et_fragment_join_room_nickname.text.toString().trim()
        val code = et_fragment_join_room_code.text.toString()
        val password = et_fragment_join_room_password.text.toString()

        if (nickname.isEmpty()) {
            mal.snack("Escribe un apodo para identificarte.")
            return
        }

        val playerDTO = PlayerDTO(nickname.toLowerCase().capitalize())

        if (roomToken != null) {
            RoomController.instance.join(playerDTO, roomToken!!)

        } else {
            if (code.isEmpty()) {
                mal.snack("Escribe el número de la sala a la que quieres unirte.")
                return
            }

            if (password.isEmpty()) {
                mal.snack("Escribe la contraseña de la sala a la que quieres unirte.")
                return
            }

            RoomController.instance.join(playerDTO, code.toInt(), password, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(joinRoomEvent: JoinRoomEvent) {
        when (RoomController.instance.getRoom().screen) {
            "lobby" -> mal.gotoLobbyFragment()
            "game" -> mal.gotoGameFragment()
            "votes" -> mal.gotoVotesFragment()
            "endgame" -> mal.gotoEndgameFragment()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    fun setInviteData(invitingNickname: String?, roomToken: String?): JoinRoomFragment {
        this.invitingNickname = invitingNickname
        this.roomToken = roomToken

        return this
    }
}
