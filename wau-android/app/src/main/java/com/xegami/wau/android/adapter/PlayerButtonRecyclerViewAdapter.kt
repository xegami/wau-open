package com.xegami.wau.android.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO
import com.xegami.wau.android.util.MyAnimationUtils
import kotlinx.android.synthetic.main.fragment_votes.*
import kotlinx.android.synthetic.main.list_row_player_button.view.*
import java.util.logging.Handler

class PlayerButtonRecyclerViewAdapter(
    private val context: Context,
    private val playersAdapterList: List<PlayerDTO>,
    private val localPlayerVotedTo: PlayerDTO?,
    private val mal: MainActivityListener,
    private val reduceFontSize: Boolean,
    private val virgin: Boolean,
    private val tvWaiting: View
) : RecyclerView.Adapter<PlayerButtonRecyclerViewAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.list_row_player_button, parent, false)
        return PlayerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playersAdapterList.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val playerDTO = playersAdapterList[position]
        holder.bind(
            playerDTO,
            localPlayerVotedTo,
            mal,
            reduceFontSize,
            position,
            virgin,
            tvWaiting
        )
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val selectors = intArrayOf(
            R.drawable.selector_player_1,
            R.drawable.selector_player_2,
            R.drawable.selector_player_3,
            R.drawable.selector_player_4,
            R.drawable.selector_player_5,
            R.drawable.selector_player_6,
            R.drawable.selector_player_7,
            R.drawable.selector_player_8,
            R.drawable.selector_player_9,
            R.drawable.selector_player_10
        )
        private val btnPlayer = itemView.btn_list_row_player_button_select_player
        private val tvNickname = itemView.tv_list_row_player_button_nickname

        fun bind(
            playerDTO: PlayerDTO,
            localPlayerVotedTo: PlayerDTO?,
            mal: MainActivityListener,
            reduceFontSize: Boolean,
            position: Int,
            virgin: Boolean,
            tvWaiting: View
        ) {
            if (position % 2 == 0){
                if (virgin) MyAnimationUtils.slideFLTR(itemView, 2000)
                (itemView.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(left = 25)
            } else {
                if (virgin) MyAnimationUtils.slideFRTL(itemView, 2000)
                (itemView.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(right = 25)
            }

            if (localPlayerVotedTo != null) {
                btnPlayer.isEnabled = false
                if (playerDTO == localPlayerVotedTo) {
                    btnPlayer.isChecked = true
                    tvNickname.updatePadding(bottom = 0)
                } else {
                    tvNickname.setTextColor(ContextCompat.getColor(itemView.context, R.color.disabled_text))
                }
            }

            tvNickname.apply {
                text = playerDTO.nickname
                if (reduceFontSize) setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            }

            btnPlayer.apply {
                background = ContextCompat.getDrawable(
                    itemView.context,
                    selectors[RoomController.instance.getRoom().players.indexOf(playerDTO)]
                )
                setOnClickListener {
                    mal.playSound("button_click")

                    btnPlayer.isEnabled = false
                    btnPlayer.isChecked = true
                    tvNickname.updatePadding(bottom = 0)

                    //tvWaiting.visibility = View.VISIBLE
                    //MyAnimationUtils.fadeIn(tvWaiting)

                    RoomController.instance.votePlayer(playerDTO)
                }
            }
        }
    }
}