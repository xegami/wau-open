package com.xegami.wau.android.util

import android.content.res.Resources
import android.view.View
import android.view.animation.*
import androidx.core.view.animation.PathInterpolatorCompat

class MyAnimationUtils {

    companion object {
        private const val ANIMATION_DURATION = 350L
        private const val ANIMATION_START_OFFSET = 250L
        private const val CHRONOBREAK_DISTANCE = 20f

        private val easeInOutInterpolator = PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f)
        private val screenWidth = Resources.getSystem().displayMetrics.widthPixels.toFloat()
        private val screenHeight = Resources.getSystem().displayMetrics.heightPixels.toFloat()

        fun fadeIn(view: View, delay: Long = 0) {
            val animation = AlphaAnimation(
                0f,
                1f
            ).apply {
                duration = ANIMATION_DURATION
                startOffset = ANIMATION_START_OFFSET + delay
            }

            view.startAnimation(animation)
        }

        fun slideFRTL(view: View, delay: Long = 0) {
            val animation = TranslateAnimation(
                screenWidth,
                -CHRONOBREAK_DISTANCE,
                0f,
                0f
            ).apply {
                duration = ANIMATION_DURATION
                startOffset = ANIMATION_START_OFFSET + delay
                interpolator = easeInOutInterpolator
                setAnimationListener(ChronobreakListener(view, fromX = -CHRONOBREAK_DISTANCE))
            }

            view.startAnimation(animation)
        }

        fun slideFLTR(view: View, delay: Long = 0) {
            val animation = TranslateAnimation(
                -screenWidth,
                CHRONOBREAK_DISTANCE,
                0f,
                0f
            ).apply {
                duration = ANIMATION_DURATION
                startOffset = ANIMATION_START_OFFSET + delay
                interpolator = easeInOutInterpolator
                setAnimationListener(ChronobreakListener(view, fromX = CHRONOBREAK_DISTANCE))
            }

            view.startAnimation(animation)
        }

        fun slideFDTU(view: View, delay: Long = 0) {
            val animation = TranslateAnimation(
                0f,
                0f,
                screenHeight,
                -CHRONOBREAK_DISTANCE
            ).apply {
                duration = ANIMATION_DURATION
                startOffset = ANIMATION_START_OFFSET + delay
                interpolator = easeInOutInterpolator
                setAnimationListener(ChronobreakListener(view, fromY = -CHRONOBREAK_DISTANCE))
            }

            view.startAnimation(animation)
        }

        fun slideFUTD(view: View, delay: Long = 0) {
            val animation = TranslateAnimation(
                0f,
                0f,
                -screenHeight,
                CHRONOBREAK_DISTANCE
            ).apply {
                duration = ANIMATION_DURATION
                startOffset = ANIMATION_START_OFFSET + delay
                interpolator = easeInOutInterpolator
                setAnimationListener(ChronobreakListener(view, fromY = CHRONOBREAK_DISTANCE))
            }

            view.startAnimation(animation)
        }

        fun rotate(view: View) {
            val animation = RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = 500
                interpolator = LinearInterpolator()
            }

            view.startAnimation(animation)
        }

        private class ChronobreakListener(
            val view: View,
            val fromX: Float = 0f,
            val fromY: Float = 0f
        ) : Animation.AnimationListener {

            private fun chronobreak(view: View, fromX: Float, fromY: Float) {
                val animation = TranslateAnimation(
                    fromX,
                    0f,
                    fromY,
                    0f
                ).apply {
                    duration = ANIMATION_DURATION / 2
                }

                view.startAnimation(animation)
            }

            override fun onAnimationRepeat(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                chronobreak(view, fromX, fromY)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        }
    }
}