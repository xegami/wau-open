package com.xegami.wau.android.activity

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.xegami.wau.android.BuildConfig
import com.xegami.wau.android.MyApp
import com.xegami.wau.android.R
import com.xegami.wau.android.fragment.*
import com.xegami.wau.android.fragment.dialog.BasicDialogFragment
import com.xegami.wau.android.fragment.dialog.SettingsDialogFragment
import com.xegami.wau.android.fragment.dialog.SkipRoundDialogFragment
import com.xegami.wau.android.listener.AppVisibilityChangeListener
import com.xegami.wau.android.listener.BasicDialogListener
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.event.*
import com.xegami.wau.android.util.MyAnimationUtils
import com.xegami.wau.android.util.MySnackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.NullPointerException
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity(), MainActivityListener, AppVisibilityChangeListener {

    private val bannedAdId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else "ca-app-pub-6956814729390389/2932561817"
    private val interstitialAdId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else "ca-app-pub-6956814729390389/7163558667"
    private var soundsMuted = false
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var bannerAd: AdView
    private var adShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyApp.instance.setOnAppVisibilityChangeListener(this)

        bannerAd = AdView(this).apply {
            adSize = AdSize.BANNER
            adUnitId = bannedAdId
            ll_activity_main_banner_ad_container.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }

        interstitialAd = InterstitialAd(this).apply {
            adUnitId = interstitialAdId
            adListener = object : AdListener() {
                override fun onAdClosed() {
                    adShowed = true
                    gotoEndgameFragment()
                }
            }
        }

        loadAds()

        vv_activity_main_background.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.bg_animation))
        vv_activity_main_background.setOnPreparedListener { mp -> mp.isLooping = true }
        vv_activity_main_background.start()

        cl_activity_main_settings.setOnClickListener { onSettingsButtonClick() }

        if (savedInstanceState == null) {
            playVideoBackground()
            gotoTitleFragment(false)
        }

        // reconnection timer
        Timer().schedule(timerTask { RoomController.instance.connect() }, 15000, 5000)

        Firebase.dynamicLinks.getDynamicLink(intent).addOnSuccessListener { data ->
            if (data != null) {
                val invitingNickname = data.link?.getQueryParameter("player")
                val roomToken = data.link?.getQueryParameter("token")
                if (invitingNickname != null && roomToken != null) {
                    gotoJoinRoomFragmentByLink(invitingNickname, roomToken)
                }
            }
        }
    }

    private fun loadAds() {
        bannerAd.loadAd(AdRequest.Builder().build())
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun onSettingsButtonClick() {
        Handler().postDelayed({ showSettingsDialogFragment() }, 400)
        MyAnimationUtils.rotate(iv_activity_main_settings)
    }

    private fun replaceFragment(fragment: Fragment, tag: String, backwards: Boolean = false) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (backwards) {
            fragmentTransaction.setCustomAnimations(
                R.anim.enter_from_left,
                R.anim.exit_to_right,
                R.anim.enter_from_right,
                R.anim.exit_to_left
            )
        } else {
            fragmentTransaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
        }

        fragmentTransaction.replace(R.id.fl_activity_main_container, fragment, tag)

        fragmentTransaction.commit()
    }

    override fun gotoTitleFragment(backwards: Boolean) {
        replaceFragment(TitleFragment(), "title", backwards)
    }

    override fun gotoMainMenuFragment(backwards: Boolean) {
        replaceFragment(MenuFragment(), "menu", backwards)
    }

    override fun gotoCreateRoomFragment(backwards: Boolean) {
        replaceFragment(CreateRoomFragment(), "createRoom", backwards)
    }

    override fun gotoJoinRoomFragment(backwards: Boolean) {
        replaceFragment(JoinRoomFragment(), "joinRoom", backwards)
    }

    private fun gotoJoinRoomFragmentByLink(invitingNickname: String, roomToken: String) {
        replaceFragment(JoinRoomFragment().setInviteData(invitingNickname, roomToken), "joinRoom")
    }

    override fun gotoLobbyFragment() {
        adShowed = false
        loadAds()
        replaceFragment(LobbyFragment(), "lobby")
    }

    override fun gotoGameFragment() {
        playSound("game")
        replaceFragment(GameFragment(), "game")
    }

    override fun gotoVotesFragment() {
        playSound("votes")
        replaceFragment(VotesFragment(), "votes")
    }

    override fun gotoEndgameFragment() {
        if (interstitialAd.isLoaded) {
            if (adShowed) {
                playSound("endgame")
                replaceFragment(EndgameFragment(), "endgame")
            } else {
                interstitialAd.show()
            }
        } else {
            playSound("endgame")
            replaceFragment(EndgameFragment(), "endgame")
        }
    }

    override fun showSkipRoundDialogFragment() {
        if (supportFragmentManager.findFragmentByTag("skip_round") == null) {
            SkipRoundDialogFragment().show(supportFragmentManager, "skip_round")
        }
    }

    private fun showSettingsDialogFragment() {
        SettingsDialogFragment().show(supportFragmentManager, "settings")
    }

    override fun snack(message: String) {
        MySnackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun showBasicDialogFragment(title: String, listener: BasicDialogListener?, displayCancelButton: Boolean) {
        BasicDialogFragment()
            .setTitle(title)
            .setListener(listener)
            .displayCancelButton(displayCancelButton)
            .show(supportFragmentManager, "info")
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("title") != null) {
            super.onBackPressed()
        } else if (supportFragmentManager.findFragmentByTag("menu") != null) {
            gotoTitleFragment(true)
        } else if (supportFragmentManager.findFragmentByTag("joinRoom") != null
            || supportFragmentManager.findFragmentByTag("createRoom") != null
        ) {
            gotoMainMenuFragment(true)
        } else {
            showBasicDialogFragment(
                "¿Estás seguro de que deseas salir?",
                object : BasicDialogListener {
                    override fun onAccept(dialog: BasicDialogFragment) {
                        RoomController.instance.leave()
                        gotoTitleFragment(true)
                    }

                    override fun onCancel(dialog: BasicDialogFragment) {
                    }

                },
                true
            )
        }
    }

    override fun onChange(isInBackground: Boolean) {
        if (isInBackground) {
            Log.e("status", "inbackground")

            RoomController.instance.disconnect()
            EventBus.getDefault().unregister(this)
        } else {
            Log.e("status", "inforeground")

            EventBus.getDefault().register(this)
            playVideoBackground()
            RoomController.instance.connect()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(joinRoomEvent: JoinRoomEvent) {
        when (RoomController.instance.getRoom().screen) {
            "lobby" -> gotoLobbyFragment()
            "game" -> gotoGameFragment()
            "votes" -> gotoVotesFragment()
            "endgame" -> gotoEndgameFragment()
        }
    }

    override fun playSound(type: String) {
        if (soundsMuted) return

        val sfxMP = when (type) {
            "button_click" -> MediaPlayer.create(this, R.raw.button_click)
            "player_join" -> MediaPlayer.create(this, R.raw.player_join)
            "player_left" -> MediaPlayer.create(this, R.raw.player_left)
            "player_voted" -> MediaPlayer.create(this, R.raw.player_voted)
            "skip_dialog" -> MediaPlayer.create(this, R.raw.skip_dialog)
            "game" -> MediaPlayer.create(this, R.raw.game)
            "votes" -> MediaPlayer.create(this, R.raw.votes)
            "endgame" -> MediaPlayer.create(this, R.raw.endgame)
            "question" -> MediaPlayer.create(this, R.raw.question)
            "pie" -> MediaPlayer.create(this, R.raw.pie)

            else -> return
        }

        sfxMP.setVolume(.5f, .5f)
        sfxMP.setOnPreparedListener { mp -> mp.start() }
        sfxMP.setOnCompletionListener { mp -> mp.release() }
    }

    private fun playVideoBackground() {
        if (!vv_activity_main_background.isPlaying) {
            vv_activity_main_background.start()
            vv_activity_main_background.visibility = View.VISIBLE
        }
    }

    override fun muteSounds(): Boolean {
        soundsMuted = !soundsMuted

        return soundsMuted
    }

    override fun areSoundsMuted(): Boolean {
        return soundsMuted
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(errorEvent: ErrorEvent) {
        snack(errorEvent.message)
    }
}
