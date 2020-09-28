package com.xegami.wau.android.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.core.view.setMargins
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.xegami.wau.android.R
import com.xegami.wau.android.adapter.PlayerLobbyRecyclerViewAdapter
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.rest.event.ErrorEvent
import com.xegami.wau.android.rest.event.GameStartedEvent
import com.xegami.wau.android.rest.event.PlayerJoinedEvent
import com.xegami.wau.android.rest.event.PlayerLeftEvent
import com.xegami.wau.android.util.MyAnimationUtils
import com.xegami.wau.android.util.MyDrawableUtils
import kotlinx.android.synthetic.main.fragment_lobby.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LobbyFragment : Fragment() {

    private lateinit var fContext: Context
    private lateinit var mal: MainActivityListener
    private val playersAdapterList = mutableListOf<PlayerDTO>()
    private lateinit var adapter: PlayerLobbyRecyclerViewAdapter
    private var inviteLink: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lobby, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        playersAdapterList.addAll(RoomController.instance.getRoom().players)

        tv_fragment_lobby_code.text = RoomController.instance.getRoom().code.toString()
        tv_fragment_lobby_password.text = RoomController.instance.getRoom().password

        if (RoomController.instance.getRoom().hotMode) {
            iv_fragment_lobby_hot_mode_flame.visibility = View.VISIBLE
            iv_fragment_lobby_hot_mode_flame.setOnClickListener { onHotModeFlameClick() }
            MyAnimationUtils.slideFUTD(iv_fragment_lobby_hot_mode_flame)
            (cl_fragment_lobby_lobby.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = 10)
        }

        btn_fragment_lobby_start_game.setOnClickListener { onStartGameClick() }
        btn_fragment_lobby_invite.setOnClickListener { onInviteClick() }

        drawPlayersViews(true)

        MyAnimationUtils.slideFUTD(cl_fragment_lobby_lobby)

        loadInviteLink()
    }

    private fun onHotModeFlameClick() {
        mal.showBasicDialogFragment(getString(R.string.hot_mode_activated_body), null, false)
    }

    private fun onStartGameClick() {
        mal.playSound("button_click")

        RoomController.instance.startGame()
    }

    private fun loadInviteLink() {
        val appUrl = Uri.parse("https://wau.com/invite?player=${RoomController.instance.getLocalPlayer().nickname}&token=${RoomController.instance.getRoom().token}")
        val firebaseUrl = "https://wau.page.link"

        FirebaseDynamicLinks.getInstance().shortLinkAsync {
            link = appUrl
            domainUriPrefix = firebaseUrl
            setAndroidParameters(DynamicLink.AndroidParameters.Builder("com.xegami.wau.android").build())
            setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder().apply {
                title = "Invitación de ${RoomController.instance.getLocalPlayer().nickname}"
                description = "${RoomController.instance.getLocalPlayer().nickname} te ha invitado a jugar a ${getString(R.string.app_name)}"
                imageUrl = Uri.parse("https://i.imgur.com/te0VE9I.png")
            }.build())
        }.addOnSuccessListener { result ->
            inviteLink = result.shortLink
        }.addOnFailureListener {
            Log.e(tag, it.message!!)
        }
    }

    private fun onInviteClick() {
        mal.playSound("button_click")

        if (inviteLink != null) {
            openShareLinkIntent(inviteLink!!)
        } else {
            ErrorEvent("Error al crear el enlace de invitación.").post()
        }
    }

    private fun openShareLinkIntent(link: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link.toString())

        startActivity(Intent.createChooser(intent, "Compartir enlace"))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(gameStartedEvent: GameStartedEvent) {
        mal.gotoGameFragment()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(playerJoinedEvent: PlayerJoinedEvent) {
        mal.playSound("player_join")
        mal.snack("${playerJoinedEvent.agent.nickname} se ha unido.")

        playersAdapterList.clear()
        playersAdapterList.addAll(RoomController.instance.getRoom().players)
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
        drawPlayersViews()
    }

    private fun drawPlayersViews(virgin: Boolean = false) {
        if (!::adapter.isInitialized) {
            rv_fragment_lobby_players_names.adapter =
                PlayerLobbyRecyclerViewAdapter(fContext, playersAdapterList)
        } else {
            adapter.notifyDataSetChanged()
        }

        if (RoomController.instance.getRoom().partyLeader == RoomController.instance.getLocalPlayer()) {
            btn_fragment_lobby_start_game.visibility = View.VISIBLE
            if (virgin) MyAnimationUtils.slideFDTU(btn_fragment_lobby_start_game)
            if (virgin) MyAnimationUtils.slideFDTU(btn_fragment_lobby_invite)
        }

        val gridLayout = gl_fragment_lobby_players_ready as GridLayout
        gridLayout.removeAllViews()

        // lights
        for (i in 0 until 10) {
            val light = ImageView(fContext)
            light.layoutParams = GridLayout.LayoutParams().apply {
                setMargins(25)
            }

            light.setImageDrawable(
                MyDrawableUtils.getPlayerLightDrawable(
                    i,
                    RoomController.instance.getRoom().players.getOrNull(i) != null
                )
            )

            gridLayout.addView(light)
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
}
