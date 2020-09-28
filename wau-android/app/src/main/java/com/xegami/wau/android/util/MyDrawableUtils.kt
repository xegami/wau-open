package com.xegami.wau.android.util

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.xegami.wau.android.MyApp
import com.xegami.wau.android.R

class MyDrawableUtils {

    companion object {
        const val LIGHT_RED = 100
        const val LIGHT_GREEN = 101
        const val LIGHT_DEFAULT = 102

        fun getPlayerLightDrawable(type: Int = -69, isOn: Boolean = false): Drawable {
            val playerLightId = when (type) {
                0 -> if (isOn) R.drawable.ic_player_1 else R.drawable.ic_player_1_off
                1 -> if (isOn) R.drawable.ic_player_2 else R.drawable.ic_player_2_off
                2 -> if (isOn) R.drawable.ic_player_3 else R.drawable.ic_player_3_off
                3 -> if (isOn) R.drawable.ic_player_4 else R.drawable.ic_player_4_off
                4 -> if (isOn) R.drawable.ic_player_5 else R.drawable.ic_player_5_off
                5 -> if (isOn) R.drawable.ic_player_6 else R.drawable.ic_player_6_off
                6 -> if (isOn) R.drawable.ic_player_7 else R.drawable.ic_player_7_off
                7 -> if (isOn) R.drawable.ic_player_8 else R.drawable.ic_player_8_off
                8 -> if (isOn) R.drawable.ic_player_9 else R.drawable.ic_player_9_off
                9 -> if (isOn) R.drawable.ic_player_10 else R.drawable.ic_player_10_off

                LIGHT_RED -> R.drawable.ic_light_red
                LIGHT_GREEN -> R.drawable.ic_light_green
                LIGHT_DEFAULT -> R.drawable.ic_light_default

                else -> R.drawable.ic_light_default
            }

            return ContextCompat.getDrawable(MyApp.instance.applicationContext, playerLightId)!!
        }

        fun getPlayerRoundedBackground(type: Int = -69): Drawable {
            val playerBackgroundId = when (type) {
                0 -> R.drawable.background_rounded_player_1
                1 -> R.drawable.background_rounded_player_2
                2 -> R.drawable.background_rounded_player_3
                3 -> R.drawable.background_rounded_player_4
                4 -> R.drawable.background_rounded_player_5
                5 -> R.drawable.background_rounded_player_6
                6 -> R.drawable.background_rounded_player_7
                7 -> R.drawable.background_rounded_player_8
                8 -> R.drawable.background_rounded_player_9
                9 -> R.drawable.background_rounded_player_10

                else -> R.drawable.background_rounded_shadow
            }

            return ContextCompat.getDrawable(
                MyApp.instance.applicationContext,
                playerBackgroundId
            )!!
        }

    }
}