package com.xegami.wau.android.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.util.MyAnimationUtils
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {

    private lateinit var fContext: Context
    private lateinit var mal: MainActivityListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mal = fContext as MainActivityListener

        btn_fragment_menu_create_room.setOnClickListener { onCreateRoomButtonClick() }
        btn_fragment_menu_join_room.setOnClickListener { onJoinRoomButtonClick() }

        MyAnimationUtils.slideFRTL(btn_fragment_menu_join_room)
        MyAnimationUtils.slideFLTR(btn_fragment_menu_create_room)
    }

    private fun onCreateRoomButtonClick() {
        mal.playSound("button_click")
        mal.gotoCreateRoomFragment(false)
    }

    private fun onJoinRoomButtonClick() {
        mal.playSound("button_click")
        mal.gotoJoinRoomFragment(false)
    }

}
