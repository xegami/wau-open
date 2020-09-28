package com.xegami.wau.android.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.xegami.wau.android.R
import com.xegami.wau.android.activity.MainActivity
import com.xegami.wau.android.adapter.PlayerKickRecyclerViewAdapter
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.listener.OnPlayerClickListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.event.GameEndedEarlyEvent
import com.xegami.wau.android.rest.event.PlayerLeftEvent
import kotlinx.android.synthetic.main.dialog_fragment_settings.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingsDialogFragment : DialogFragment(), OnPlayerClickListener {

    private lateinit var mal: MainActivityListener
    private lateinit var fContext: Context
    private lateinit var rootView: View
    private val playersAdapterList = mutableListOf<PlayerDTO>()
    private lateinit var target: PlayerDTO

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView =
            LayoutInflater.from(fContext).inflate(R.layout.dialog_fragment_settings, null)

        return AlertDialog.Builder(fContext).apply {
            setView(rootView)
        }.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mal = fContext as MainActivityListener

        if (mal.areSoundsMuted()) {
            tv_dialog_fragment_settings_menu_mute_label.text = "Activar sonido"
            iv_dialog_fragment_settings_menu_mute.setImageDrawable(ContextCompat.getDrawable(fContext, R.drawable.ic_mute_off))
        } else {
            tv_dialog_fragment_settings_menu_mute_label.text = "Quitar sonido"
            iv_dialog_fragment_settings_menu_mute.setImageDrawable(ContextCompat.getDrawable(fContext, R.drawable.ic_mute_on))
        }

        if (!RoomController.instance.isInRoom() || RoomController.instance.getRoom().partyLeader != RoomController.instance.getLocalPlayer())
            cl_dialog_fragment_settings_menu_kick.visibility = View.GONE

        cl_dialog_fragment_settings_menu_mute.setOnClickListener { onMenuMuteClick() }
        cl_dialog_fragment_settings_menu_kick.setOnClickListener { onMenuKickPlayerClick() }
        cl_dialog_fragment_settings_menu_about.setOnClickListener { onMenuAboutClick() }
        cl_dialog_fragment_settings_menu_leave.setOnClickListener { onMenuLeaveClick() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    private fun onMenuMuteClick() {
        if (mal.muteSounds()) {
            tv_dialog_fragment_settings_menu_mute_label.text = "Activar sonido"
            iv_dialog_fragment_settings_menu_mute.setImageDrawable(ContextCompat.getDrawable(fContext, R.drawable.ic_mute_off))
        } else {
            tv_dialog_fragment_settings_menu_mute_label.text = "Quitar sonido"
            iv_dialog_fragment_settings_menu_mute.setImageDrawable(ContextCompat.getDrawable(fContext, R.drawable.ic_mute_on))

        }
    }

    private fun onMenuKickPlayerClick() {
        cl_dialog_fragment_settings_menu.visibility = View.GONE
        cl_dialog_fragment_settings_kick_player.visibility = View.VISIBLE

        playersAdapterList.clear()
        playersAdapterList.addAll(RoomController.instance.getRoom().players)
        playersAdapterList.remove(RoomController.instance.getLocalPlayer()) // you can't vote yourself
        rv_dialog_fragment_settings_kick_player_players.adapter = PlayerKickRecyclerViewAdapter(fContext, playersAdapterList, this)

        btn_dialog_fragment_settings_accept.visibility = View.GONE
        btn_dialog_fragment_settings_cancel.visibility = View.VISIBLE

        btn_dialog_fragment_settings_cancel.setOnClickListener {
            mal.playSound("button_click")

            cl_dialog_fragment_settings_kick_player.visibility = View.GONE
            cl_dialog_fragment_settings_menu.visibility = View.VISIBLE

            btn_dialog_fragment_settings_accept.visibility = View.GONE
            btn_dialog_fragment_settings_cancel.visibility = View.GONE
        }
    }

    override fun onPlayerClick(playerDTO: PlayerDTO) {
        cl_dialog_fragment_settings_kick_player.visibility = View.GONE
        cl_dialog_fragment_settings_kick_player_confirm.visibility = View.VISIBLE

        btn_dialog_fragment_settings_accept.visibility = View.VISIBLE
        btn_dialog_fragment_settings_cancel.visibility = View.VISIBLE

        btn_dialog_fragment_settings_accept.setOnClickListener {
            mal.playSound("button_click")

            btn_dialog_fragment_settings_accept.isEnabled = false
            RoomController.instance.kick(playerDTO)
        }

        btn_dialog_fragment_settings_cancel.setOnClickListener {
            mal.playSound("button_click")

            cl_dialog_fragment_settings_kick_player_confirm.visibility = View.GONE
            cl_dialog_fragment_settings_kick_player.visibility = View.VISIBLE

            btn_dialog_fragment_settings_accept.visibility = View.GONE
            btn_dialog_fragment_settings_cancel.visibility = View.VISIBLE

            btn_dialog_fragment_settings_cancel.setOnClickListener {
                mal.playSound("button_click")

                cl_dialog_fragment_settings_kick_player.visibility = View.GONE
                cl_dialog_fragment_settings_menu.visibility = View.VISIBLE

                btn_dialog_fragment_settings_accept.visibility = View.GONE
                btn_dialog_fragment_settings_cancel.visibility = View.GONE
            }
        }

        tv_dialog_fragment_settings_kick_player_confirm_title.text = "¿Expulsar a ${playerDTO.nickname}?"
        if (RoomController.instance.getRoom().players.size == 3) tv_dialog_fragment_settings_kick_player_confirm_alert.visibility = View.VISIBLE

        target = playerDTO
    }

    private fun onMenuAboutClick() {
        cl_dialog_fragment_settings_menu.visibility = View.GONE
        cl_dialog_fragment_settings_about.visibility = View.VISIBLE

        btn_dialog_fragment_settings_accept.visibility = View.VISIBLE
        btn_dialog_fragment_settings_cancel.visibility = View.GONE

        btn_dialog_fragment_settings_accept.setOnClickListener {
            mal.playSound("button_click")

            cl_dialog_fragment_settings_about.visibility = View.GONE
            cl_dialog_fragment_settings_menu.visibility = View.VISIBLE

            btn_dialog_fragment_settings_accept.visibility = View.GONE
            btn_dialog_fragment_settings_cancel.visibility = View.GONE
        }
    }

    private fun onMenuLeaveClick() {
        cl_dialog_fragment_settings_menu.visibility = View.GONE
        cl_dialog_fragment_settings_leave.visibility = View.VISIBLE

        btn_dialog_fragment_settings_accept.visibility = View.VISIBLE
        btn_dialog_fragment_settings_cancel.visibility = View.VISIBLE

        btn_dialog_fragment_settings_accept.setOnClickListener {
            mal.playSound("button_click")

            if (RoomController.instance.isInRoom()) {
                RoomController.instance.leave()
                mal.gotoTitleFragment(true)
            } else {
                (fContext as MainActivity).finish()
            }

            dismiss()
        }

        btn_dialog_fragment_settings_cancel.setOnClickListener {
            mal.playSound("button_click")

            cl_dialog_fragment_settings_leave.visibility = View.GONE
            cl_dialog_fragment_settings_menu.visibility = View.VISIBLE

            btn_dialog_fragment_settings_accept.visibility = View.GONE
            btn_dialog_fragment_settings_cancel.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerLeftEvent: PlayerLeftEvent) {
        if (playerLeftEvent.agent == target) dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(gameEndedEarlyEvent: GameEndedEarlyEvent) {
        if (gameEndedEarlyEvent.agent == target) dismiss()
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