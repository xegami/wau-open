package com.xegami.wau.android.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.xegami.wau.android.R
import com.xegami.wau.android.MyApp

class MyMiscUtils {

    companion object {

        fun hideSoftKeyboard(context: Context, view: View) {
            val inputMethodManager =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }
}