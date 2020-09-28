package com.xegami.wau.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xegami.wau.android.MyApp
import com.xegami.wau.android.R
import com.xegami.wau.android.listener.OnPlayerClickListener
import com.xegami.wau.android.rest.dto.PlayerDTO

class PlayerKickRecyclerViewAdapter(
    private val context: Context,
    private val players: List<PlayerDTO>,
    private val listener: OnPlayerClickListener
) :
    RecyclerView.Adapter<PlayerKickRecyclerViewAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_row_kick_player, parent, false)
        return PlayerViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val playerDTO = players[position]
        holder.bind(playerDTO, position)
    }

    class PlayerViewHolder(itemView: View, val listener: OnPlayerClickListener) : RecyclerView.ViewHolder(itemView) {

        private val tvNickname = itemView.findViewById<TextView>(R.id.tv_list_row_kick_player_nickname)
        private val vUnderline = itemView.findViewById<View>(R.id.v_list_row_kick_player_underline)

        fun bind(playerDTO: PlayerDTO, position: Int) {
            tvNickname.text = playerDTO.nickname
            itemView.setOnClickListener { listener.onPlayerClick(playerDTO) }

            vUnderline.background = when (position) {
                0 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_2)
                1 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_3)
                2 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_4)
                3 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_5)
                4 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_6)
                5 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_7)
                6 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_8)
                7 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_9)
                8 -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_10)
                else -> ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_10)
            }
        }

    }

}