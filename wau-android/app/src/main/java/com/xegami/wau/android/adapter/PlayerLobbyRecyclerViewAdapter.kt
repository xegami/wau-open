package com.xegami.wau.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xegami.wau.android.MyApp
import com.xegami.wau.android.R
import com.xegami.wau.android.rest.controller.RoomController
import com.xegami.wau.android.rest.dto.PlayerDTO

class PlayerLobbyRecyclerViewAdapter(
    private val context: Context,
    private val players: List<PlayerDTO>
) :
    RecyclerView.Adapter<PlayerLobbyRecyclerViewAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_row_lobby_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val playerDTO = players[position]
        holder.bind(position, playerDTO)
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNickname = itemView.findViewById<TextView>(R.id.tv_list_row_lobby_player_nickname)
        private val ivPartyOwner =
            itemView.findViewById<ImageView>(R.id.iv_list_row_lobby_player_party_owner)
        private val vUnderline =
            itemView.findViewById<View>(R.id.v_list_row_lobby_player_underline)

        fun bind(position: Int, playerDTO: PlayerDTO) {
            tvNickname.text = playerDTO.nickname

            if (playerDTO == RoomController.instance.getLocalPlayer()) {
                tvNickname.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange))
                ivPartyOwner.setColorFilter(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.orange
                    )
                )
            }

            when (position) {
                0 -> {
                    vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_1)
                    ivPartyOwner.visibility = View.VISIBLE
                }
                1 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_2)
                2 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_3)
                3 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_4)
                4 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_5)
                5 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_6)
                6 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_7)
                7 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_8)
                8 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_9)
                9 -> vUnderline.background = ContextCompat.getDrawable(MyApp.instance.applicationContext, R.drawable.background_player_10)
            }
        }
    }

}