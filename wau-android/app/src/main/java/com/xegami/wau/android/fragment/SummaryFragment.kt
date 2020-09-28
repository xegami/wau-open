package com.xegami.wau.android.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xegami.wau.android.R
import com.xegami.wau.android.adapter.CommentRecyclerViewAdapter
import com.xegami.wau.android.listener.MainActivityListener
import com.xegami.wau.android.rest.dto.PlayerSummaryDTO
import com.xegami.wau.android.util.MyDrawableUtils
import kotlinx.android.synthetic.main.fragment_summary.*

class SummaryFragment : Fragment() {

    private lateinit var fContext: Context
    private lateinit var playerSummaryDTO: PlayerSummaryDTO

    companion object {
        fun newInstance(playerSummaryDTO: PlayerSummaryDTO): SummaryFragment {
            val fragment = SummaryFragment()
            val bundle = Bundle()

            bundle.putSerializable("comments", playerSummaryDTO)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        playerSummaryDTO = arguments!!.getSerializable("comments") as PlayerSummaryDTO

        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setBackgroundColor(Color.TRANSPARENT)

        cl_fragment_summary_card.background = MyDrawableUtils.getPlayerRoundedBackground(playerSummaryDTO.index)
        tv_fragment_summary_player_nickname.text = playerSummaryDTO.nickname
        tv_fragment_summary_vote_count.text = playerSummaryDTO.comments.size.toString() + if (playerSummaryDTO.comments.size == 1) " voto" else " votos"
        rv_fragment_summary_comments.setHasFixedSize(true)
        rv_fragment_summary_comments.layoutManager = LinearLayoutManager(fContext)
        rv_fragment_summary_comments.adapter = CommentRecyclerViewAdapter(fContext, playerSummaryDTO.comments)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context
    }

}