package com.xegami.wau.android.listener

interface MainActivityListener {

    fun gotoTitleFragment(backwards: Boolean)

    fun gotoMainMenuFragment(backwards: Boolean)

    fun gotoCreateRoomFragment(backwards: Boolean)

    fun gotoJoinRoomFragment(backwards: Boolean)

    fun gotoLobbyFragment()

    fun gotoGameFragment()

    fun gotoVotesFragment()

    fun gotoEndgameFragment()

    fun showSkipRoundDialogFragment()

    fun snack(message: String)

    fun showBasicDialogFragment(title: String, listener: BasicDialogListener?, displayCancelButton: Boolean)

    fun playSound(type: String)

    fun muteSounds(): Boolean

    fun areSoundsMuted(): Boolean
}