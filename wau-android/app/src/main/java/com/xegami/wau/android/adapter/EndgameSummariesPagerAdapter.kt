package com.xegami.wau.android.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.xegami.wau.android.fragment.SummaryFragment
import com.xegami.wau.android.rest.dto.PlayerSummaryDTO

class EndgameSummariesPagerAdapter(fragmentManager: FragmentManager, private val playersSummaries: List<PlayerSummaryDTO>) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val endgameCommentFragmentList = mutableListOf<SummaryFragment>()

    init {
        playersSummaries.forEach {
            endgameCommentFragmentList.add(SummaryFragment.newInstance(it))
        }
    }

    override fun getItem(position: Int): Fragment {
        return endgameCommentFragmentList[position]
    }

    override fun getCount(): Int {
        return playersSummaries.size
    }

}