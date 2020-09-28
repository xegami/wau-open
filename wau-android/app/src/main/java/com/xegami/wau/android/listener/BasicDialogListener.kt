package com.xegami.wau.android.listener

import com.xegami.wau.android.fragment.dialog.BasicDialogFragment

interface BasicDialogListener {

    fun onAccept(dialog: BasicDialogFragment)

    fun onCancel(dialog: BasicDialogFragment)

}