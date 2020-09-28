package com.xegami.wau.android.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.xegami.wau.android.R
import com.xegami.wau.android.adapter.PlayerButtonRecyclerViewAdapter
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.event.*
import com.xegami.wau.android.util.MyAnimationUtils
import com.xegami.wau.android.util.MyDrawableUtils
import kotlinx.android.synthetic.main.fragment_game.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GameFragment : Fragment() {

    private lateinit var mal: MainActivityListener
    private lateinit var fContext: Context
    private val playersAdapterList = mutableListOf<PlayerDTO>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        playersAdapterList.addAll(RoomController.instance.getRoom().players)
        playersAdapterList.remove(RoomController.instance.getLocalPlayer()) // you can't vote yourself

        btn_fragment_game_skip.setOnClickListener { onSkipVoteButtonClick() }
        view_fragment_game_anti_touch.setOnClickListener {}

        // game info
        tv_fragment_game_code.text = RoomController.instance.getRoom().code.toString()
        tv_fragment_game_password.text = RoomController.instance.getRoom().password
        tv_fragment_game_round.text = "${RoomController.instance.getRoom().roundNumber + 1} / 20"

        // game question
        val question =
            RoomController.instance.getRoom().rounds[RoomController.instance.getRoom().roundNumber].question
        tv_fragment_game_question_number.text = question.id.toString()
        tv_fragment_game_question_body_beginning.text = question.body.split("|")[0] + "..."
        tv_fragment_game_question_body_end.text = question.body.split("|")[1]

        drawPlayersViews(true)

        Handler().postDelayed({view_fragment_game_anti_touch.visibility = View.GONE}, 2250)
        Handler().postDelayed({mal.playSound("question")}, 250)
        MyAnimationUtils.slideFUTD(cl_fragment_game_question)
        MyAnimationUtils.slideFDTU(btn_fragment_game_skip, 2000)
    }

    private fun onSkipVoteButtonClick() {
        btn_fragment_game_skip.isEnabled = false

        RoomController.instance.voteSkip()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerVotedEvent: PlayerVotedEvent) {
        if (playerVotedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(roundEndedEvent: RoundEndedEvent) {
        if (roundEndedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
        Handler().postDelayed({mal.gotoVotesFragment()}, 300)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerJoinedEvent: PlayerJoinedEvent) {
        mal.playSound("player_join")
        mal.snack("${playerJoinedEvent.agent.nickname} se ha unido.")

        playersAdapterList.clear()
        playersAdapterList.addAll(RoomController.instance.getRoom().players)
        playersAdapterList.remove(RoomController.instance.getLocalPlayer()) // you can't vote yourself

        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerLeftEvent: PlayerLeftEvent) {
        mal.playSound("player_left")

        if (playerLeftEvent.agent == RoomController.instance.getLocalPlayer() && playerLeftEvent.kicked) {
            mal.snack("Has sido expulsado de la sala.")
            mal.gotoTitleFragment(true)
            return
        } else {
            mal.snack(playerLeftEvent.agent.nickname + if (playerLeftEvent.kicked) " ha sido expulsado." else " se ha ido.")
        }

        playersAdapterList.clear()
        playersAdapterList.addAll(RoomController.instance.getRoom().players)
        playersAdapterList.remove(RoomController.instance.getLocalPlayer()) // you can't vote yourself

        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(gameEndedEarlyEvent: GameEndedEarlyEvent) {
        if (gameEndedEarlyEvent.agent == RoomController.instance.getLocalPlayer() && gameEndedEarlyEvent.kicked) {
            mal.snack("Has sido expulsado de la sala.")
            mal.gotoTitleFragment(true)
            return
        } else {
            mal.gotoEndgameFragment()
            mal.showBasicDialogFragment("La partida terminó porque ${gameEndedEarlyEvent.agent.nickname}" + (if (gameEndedEarlyEvent.kicked) " fue expulsado" else " se salió") + " y ya no cumplís con el mínimo de jugadores.", null, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(skipVotedEvent: SkipVotedEvent) {
        btn_fragment_game_skip.isEnabled = false

        if (RoomController.instance.getRoom().votingSkipPlayers.isNotEmpty()) {
            if (skipVotedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("skip_dialog")
            mal.showSkipRoundDialogFragment()
        }
    }

    private fun drawPlayersViews(virgin: Boolean = false) {
        // buttons
        var localPlayerVotedTo: PlayerDTO? = null
        RoomController.instance.getRoom().rounds[RoomController.instance.getRoom().roundNumber].votedPlayers.forEach { votedPlayer ->
            votedPlayer.voting.forEach { votingPlayer ->
                if (votingPlayer == RoomController.instance.getLocalPlayer()) {
                    localPlayerVotedTo = votedPlayer.voted
                }
            }
        }

        if (localPlayerVotedTo != null) btn_fragment_game_skip.isEnabled = false

        val spanCount = when (RoomController.instance.getRoom().players.size) {
            10 -> 3
            else -> 2
        }

        rv_fragment_game_players_buttons.layoutManager = GridLayoutManager(fContext, spanCount)
        rv_fragment_game_players_buttons.adapter = PlayerButtonRecyclerViewAdapter(fContext, playersAdapterList, localPlayerVotedTo, mal, spanCount == 3, virgin, tv_fragment_game_waiting)

        // lights
        val gridLayout = gl_fragment_game_players_ready as GridLayout
        gridLayout.removeAllViews()

        RoomController.instance.getRoom().players.forEachIndexed { index, _ ->
            val light = ImageView(fContext)
            light.layoutParams = GridLayout.LayoutParams().apply {
                setMargins(25)
            }
            light.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable(index, false))
            gridLayout.addView(light)
            if (virgin) MyAnimationUtils.fadeIn(light, 2000L + index * 150)
        }

        RoomController.instance.getRoom().rounds[RoomController.instance.getRoom().roundNumber].votedPlayers.forEach { votedPlayer ->
            votedPlayer.voting.forEach { votingPlayer ->
                val index = RoomController.instance.getRoom().players.indexOf(votingPlayer)
                val playerLight = gridLayout.getChildAt(index) as ImageView
                playerLight.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable(index, true))
            }
        }

        if (RoomController.instance.getRoom().votingSkipPlayers.isNotEmpty()) {
            mal.showSkipRoundDialogFragment()
        }
    }

}