package com.xegami.wau.android.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xegami.wau.android.R
import com.xegami.wau.android.activity.MainActivity
import com.xegami.wau.android.adapter.EndgameSummariesPagerAdapter
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerSummaryDTO
import com.xegami.wau.android.rest.event.PlayAgainEvent
import com.xegami.wau.android.rest.event.PlayerJoinedEvent
import com.xegami.wau.android.rest.event.PlayerLeftEvent
import com.xegami.wau.android.util.MyAnimationUtils
import kotlinx.android.synthetic.main.fragment_endgame.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EndgameFragment : Fragment() {

    private lateinit var mal: MainActivityListener
    private lateinit var fContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_endgame, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        val playersSummaries = getPlayersSummaries()

        if (playersSummaries.isNotEmpty()) {
            vp_fragment_endgame_summaries.pageMargin = 40
            vp_fragment_endgame_summaries.adapter = EndgameSummariesPagerAdapter(
                (fContext as MainActivity).supportFragmentManager,
                playersSummaries
            )
        } else {
            vp_fragment_endgame_summaries.visibility = View.GONE
            tv_fragment_endgame_no_comments.visibility = View.VISIBLE
        }

        if (RoomController.instance.getLocalPlayer() == RoomController.instance.getRoom().partyLeader) {
            showPlayAgainButton(true)
        }

        MyAnimationUtils.slideFUTD(vp_fragment_endgame_summaries)
    }

    private fun getPlayersSummaries(): List<PlayerSummaryDTO> {
        val playersSummaries = mutableListOf<PlayerSummaryDTO>()

        for (r in RoomController.instance.getRoom().rounds) {
            if (r.votedPlayers.size == 1 || r.votedPlayers.size > 1 && r.votedPlayers[0].voting.size > r.votedPlayers[1].voting.size) {
                val index =
                    RoomController.instance.getRoom().players.indexOf(r.votedPlayers[0].voted)
                val nickname = r.votedPlayers[0].voted.nickname
                val comment = r.question.comment.replace("%", nickname)

                if (playersSummaries.find { p -> p.index == index } == null) {
                    val playerSummary = PlayerSummaryDTO(
                        if (index == -1) RoomController.instance.getRoom().players.size else index,
                        nickname
                    ).apply {
                        comments.add(comment)
                    }

                    playersSummaries.add(playerSummary)
                } else {
                    playersSummaries.find { p -> p.index == index }!!.comments.add(comment)
                }
            }
        }

        playersSummaries.sortByDescending { playerSummaryDTO -> playerSummaryDTO.comments.size }

        return playersSummaries
    }

    private fun onPlayAgainButtonClick() {
        mal.playSound("button_click")

        RoomController.instance.playAgain()
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
    fun onEvent(playAgainEvent: PlayAgainEvent) {
        mal.gotoLobbyFragment()
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

        if (RoomController.instance.getLocalPlayer() == RoomController.instance.getRoom().partyLeader) {
            showPlayAgainButton()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerJoinedEvent: PlayerJoinedEvent) {
        mal.playSound("player_join")
        mal.snack("${playerJoinedEvent.agent.nickname} se ha unido.")
    }

    private fun showPlayAgainButton(virgin: Boolean = false) {
        btn_fragment_endgame_play_again.visibility = View.VISIBLE
        btn_fragment_endgame_play_again.setOnClickListener { onPlayAgainButtonClick() }
        if (virgin) MyAnimationUtils.slideFDTU(btn_fragment_endgame_play_again)
    }

}