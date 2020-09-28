package com.xegami.wau.android.fragment

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.model.GradientColor
import com.github.mikephil.charting.utils.ColorTemplate.colorWithAlpha
import com.github.mikephil.charting.utils.ColorTemplate.rgb
import com.xegami.wau.android.MyApp
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.event.*
import com.xegami.wau.android.util.MyAnimationUtils
import com.xegami.wau.android.util.MyDrawableUtils
import com.xegami.wau.android.util.MyValueFormatter
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_votes.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VotesFragment : Fragment() {

    private lateinit var mal: MainActivityListener
    private lateinit var fContext: Context

    private val gradientColors: List<GradientColor> = listOf(
        GradientColor(rgb("#7b3eff"), rgb("#5cffd4")),
        GradientColor(rgb("#cdff31"), rgb("#4affdc")),
        GradientColor(rgb("#ff17c3"), rgb("#ffee1f")),
        GradientColor(rgb("#5440ff"), rgb("#c048ff")),
        GradientColor(rgb("#ff36f1"), rgb("#66fbff")),
        GradientColor(rgb("#ff6700"), rgb("#ffff3b")),
        GradientColor(rgb("#3eff6b"), rgb("#ffff43")),
        GradientColor(rgb("#ff3366"), rgb("#cc66ff")),
        GradientColor(rgb("#ff1717"), rgb("#ffb92c")),
        GradientColor(rgb("#2c55ff"), rgb("#3fecff"))
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_votes, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        drawPieChart()
        drawPlayersViews(true)

        // game info
        tv_fragment_votes_code.text = RoomController.instance.getRoom().code.toString()
        tv_fragment_votes_password.text = RoomController.instance.getRoom().password
        tv_fragment_votes_round.text = "${RoomController.instance.getRoom().roundNumber + 1} / 20"

        val round =
            RoomController.instance.getRoom().rounds[RoomController.instance.getRoom().roundNumber]
        if (round.votedPlayers.size > 1 && round.votedPlayers[0].voting.size <= round.votedPlayers[1].voting.size) {
            tv_fragment_votes_comment.text = "No habéis coincidido mucho esta ronda."
        } else {
            tv_fragment_votes_comment.text = round.question.comment.replace("%", round.votedPlayers[0].voted.nickname)
        }

        btn_fragment_votes_next.setOnClickListener { v -> onNextButtonClick(v) }
        view_fragment_votes_anti_touch.setOnClickListener {}

        pie_chart_fragment_votes.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        Handler().postDelayed({
            view_fragment_votes_anti_touch.visibility = View.GONE
        }, 3250)

        Handler().postDelayed({
            gl_fragment_votes_players_ready.visibility = View.VISIBLE
            tv_fragment_votes_comment.visibility = View.VISIBLE
            btn_fragment_votes_next.visibility = View.VISIBLE

            MyAnimationUtils.slideFDTU(tv_fragment_votes_comment)
            MyAnimationUtils.slideFDTU(btn_fragment_votes_next, 1000)

        }, 2250)
    }

    private fun onNextButtonClick(v: View) {
        v.isEnabled = false
        mal.playSound("button_click")

        //tv_fragment_votes_waiting.visibility = View.VISIBLE
        //MyAnimationUtils.fadeIn(tv_fragment_votes_waiting)

        RoomController.instance.voteNext()
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
    fun onEvent(nextVotedEvent: NextVotedEvent) {
        if (nextVotedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")

        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerJoinedEvent: PlayerJoinedEvent) {
        mal.playSound("player_join")
        mal.snack("${playerJoinedEvent.agent.nickname} se ha unido.")

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

        drawPlayersViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(gameEndedEvent: GameEndedEvent) {
        if (gameEndedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
        Handler().postDelayed({mal.gotoEndgameFragment()}, 300)
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
    fun onEvent(votesEndedEvent: VotesEndedEvent) {
        if (votesEndedEvent.agent != RoomController.instance.getLocalPlayer()) mal.playSound("player_voted")
        drawPlayersViews()
        Handler().postDelayed({mal.gotoGameFragment()}, 300)
    }

    private fun drawPieChart() {
        // chart entries
        val entries = mutableListOf<PieEntry>()
        RoomController.instance.getRoom().rounds[RoomController.instance.getRoom().roundNumber].votedPlayers.forEach {
            val index = RoomController.instance.getRoom().players.indexOf(it.voted)
            entries.add(
                PieEntry(
                    it.voting.size.toFloat(),
                    it.voted.nickname,
                    if (index == -1) RoomController.instance.getRoom().players.size else index
                )
            )
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 5F
        dataSet.gradientColors = gradientColors

        val data = PieData(dataSet)
        data.setValueTextSize(14F)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(MyApp.appTypeface)
        data.setValueFormatter(MyValueFormatter())
        pie_chart_fragment_votes.data = data

        // chart properties
        pie_chart_fragment_votes.legend.isEnabled = false
        pie_chart_fragment_votes.description.isEnabled = false
        pie_chart_fragment_votes.isHighlightPerTapEnabled = false
        pie_chart_fragment_votes.setEntryLabelTypeface(MyApp.appTypeface)
        pie_chart_fragment_votes.setEntryLabelTextSize(20F)
        pie_chart_fragment_votes.dragDecelerationFrictionCoef = 0.95F
        pie_chart_fragment_votes.holeRadius = 20F
        pie_chart_fragment_votes.setHoleColor(colorWithAlpha(rgb("#1c232b"), 175))
        pie_chart_fragment_votes.transparentCircleRadius = 0F
        pie_chart_fragment_votes.animateY(2000, Easing.EaseInOutCubic)
        mal.playSound("pie")
    }

    private fun drawPlayersViews(virgin: Boolean = false) {
        val gridLayout = gl_fragment_votes_players_ready as GridLayout
        gridLayout.removeAllViews()

        // lights
        RoomController.instance.getRoom().players.forEachIndexed { index, _ ->
            val light = ImageView(fContext)
            light.layoutParams = GridLayout.LayoutParams().apply {
                setMargins(25)
            }
            light.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable(index, false))
            gridLayout.addView(light)
            if (virgin) MyAnimationUtils.fadeIn(light, 1000L + index * 150)
        }

        RoomController.instance.getRoom().votingNextPlayers.forEach { votingNextPlayer ->
            val index = RoomController.instance.getRoom().players.indexOf(votingNextPlayer)
            val playerLight = gridLayout.getChildAt(index) as ImageView
            playerLight.setImageDrawable(MyDrawableUtils.getPlayerLightDrawable(index, true))
        }
    }
}