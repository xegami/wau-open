package com.xegami.wau.android.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.view.setMargins
import androidx.fragment.app.DialogFragment
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.event.*
import com.xegami.wau.android.util.MyDrawableUtils
import kotlinx.android.synthetic.main.dialog_fragment_skip_round.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SkipRoundDialogFragment : DialogFragment() {

    private lateinit var mal: MainActivityListener
    private lateinit var fContext: Context
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView =
            LayoutInflater.from(fContext).inflate(R.layout.dialog_fragment_skip_round, null)

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

        isCancelable = false

        if (RoomController.instance.getRoom().votingSkipPlayers.contains(RoomController.instance.getLocalPlayer())) {
            btn_dialog_fragment_skip_round_accept.isChecked = true
            disableButtons()
        }

        btn_dialog_fragment_skip_round_accept.setOnClickListener { v ->
            onSkipRoundAcceptButtonClick(
                v
            )
        }
        btn_dialog_fragment_skip_round_cancel.setOnClickListener { v ->
            onSkipRoundCancelButtonClick(
                v
            )
        }

        drawPlayersViews()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    private fun onSkipRoundAcceptButtonClick(view: View) {
        mal.playSound("button_click")

        disableButtons()
        //tv_dialog_fragment_skip_round_waiting.visibility = View.VISIBLE
        //MyAnimationUtils.fadeIn(tv_dialog_fragment_skip_round_waiting)

        RoomController.instance.voteSkip()
    }

    private fun disableButtons() {
        btn_dialog_fragment_skip_round_accept.isEnabled = false
        btn_dialog_fragment_skip_round_cancel.isEnabled = false
    }

    private fun onSkipRoundCancelButtonClick(view: View) {
        mal.playSound("button_click")

        disableButtons()
        //tv_dialog_fragment_skip_round_waiting.visibility = View.VISIBLE
        //MyAnimationUtils.fadeIn(tv_dialog_fragment_skip_round_waiting)

        RoomController.instance.voteCancelSkip()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(skipVotedEvent: SkipVotedEvent) {
        if (skipVotedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(cancelSkipVotedEvent: CancelSkipVotedEvent) {
        if (cancelSkipVotedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(skipNotRoundEvent: SkipNotRoundEvent) {
        if (skipNotRoundEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
        mal.snack("No se ha pasado la pregunta.")

        Handler().postDelayed({dismiss()}, 300)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(skipRoundEvent: SkipRoundEvent) {
        if (skipRoundEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
        mal.snack("Se ha pasado la pregunta.")

        Handler().postDelayed({
            mal.gotoGameFragment()
            dismiss()
        }, 300)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(gameEndedEvent: GameEndedEvent) {
        drawPlayersViews()
        mal.snack("Se ha pasado la última pregunta.")

        Handler().postDelayed({
            mal.gotoEndgameFragment()
            dismiss()
        }, 300)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(gameEndedEarlyEvent: GameEndedEarlyEvent) {
        dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerJoinedEvent: PlayerJoinedEvent) {
        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerLeftEvent: PlayerLeftEvent) {
        if (playerLeftEvent.agent == RoomController.instance.getLocalPlayer() && playerLeftEvent.kicked) {
            dismiss()
            return
        }

        drawPlayersViews()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    private fun drawPlayersViews() {
        val gridLayout = gl_dialog_fragment_skip_round_players_ready as GridLayout

        gridLayout.removeAllViews()

        // lights
        RoomController.instance.getRoom().players.forEach { _ ->
            val light = ImageView(fContext)
            light.layoutParams = GridLayout.LayoutParams().apply {
                setMargins(25)
            }
            light.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable())
            gridLayout.addView(light)
        }

        for (i in RoomController.instance.getRoom().votingSkipPlayers.indices) {
            val playerLight = gridLayout.getChildAt(i) as ImageView
            playerLight.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable(MyDrawableUtils.LIGHT_GREEN))
        }

        for (i in RoomController.instance.getRoom().votingCancelSkipPlayers.indices) {
            val playerLight = gridLayout.getChildAt(i + RoomController.instance.getRoom().votingSkipPlayers.size) as ImageView
            playerLight.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable(MyDrawableUtils.LIGHT_RED))
        }

    }

}