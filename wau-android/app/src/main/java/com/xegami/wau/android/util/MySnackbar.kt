package com.xegami.wau.android.util

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.xegami.wau.android.R
import kotlinx.android.synthetic.main.my_snackbar.view.*

class MySnackbar private constructor(
    parent: ViewGroup,
    content: View,
    callback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<MySnackbar>(parent, content, callback) {

    companion object {
        fun make(parent: ViewGroup, text: String, @Duration duration: Int): MySnackbar {
            val inflater = LayoutInflater.from(parent.context)
            val content = inflater.inflate(R.layout.my_snackbar, parent, false)
            val mySnackbar = MySnackbar(parent, content, object : ContentViewCallback {
                override fun animateContentOut(delay: Int, duration: Int) {
                }

                override fun animateContentIn(delay: Int, duration: Int) {
                }
            })

            mySnackbar.view.setBackgroundColor(Color.TRANSPARENT)
            mySnackbar.view.tv_my_snackbar_text.text = text
            mySnackbar.duration = duration

            return mySnackbar
        }
    }
}