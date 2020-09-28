package com.xegami.wau.android.fragment.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.BasicDialogListener
import com.xegami.wau.android.listener.MainActivityListener
import kotlinx.android.synthetic.main.dialog_fragment_basic.*

class BasicDialogFragment : DialogFragment() {

    private lateinit var mal: MainActivityListener
    private lateinit var fContext: Context
    private lateinit var rootView: View
    private lateinit var title: String
    private lateinit var listener: BasicDialogListener
    private var displayCancelButton: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView =
            LayoutInflater.from(fContext).inflate(R.layout.dialog_fragment_basic, null)

        return AlertDialog.Builder(fContext).apply {
            setView(rootView)
        }.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mal = fContext as MainActivityListener

        tv_dialog_fragment_basic_title.text = title

        btn_dialog_fragment_basic_accept.setOnClickListener {
            mal.playSound("button_click")
            if (::listener.isInitialized) listener.onAccept(this)
            dismiss()
        }

        btn_dialog_fragment_basic_cancel.setOnClickListener {
            mal.playSound("button_click")
            if (::listener.isInitialized) listener.onCancel(this)
            dismiss()
        }

        btn_dialog_fragment_basic_cancel.visibility = if (displayCancelButton) View.VISIBLE else View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    fun setTitle(title: String): BasicDialogFragment {
        this.title = title

        return this
    }

    fun setListener(listener: BasicDialogListener?): BasicDialogFragment {
        if (listener != null) {
            this.listener = listener
        }

        return this
    }

    fun displayCancelButton(displayCancelButton: Boolean): BasicDialogFragment {
        this.displayCancelButton = displayCancelButton

        return this
    }
}