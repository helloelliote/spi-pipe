package kr.djspi.pipe01.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabAdapter(fragmentManager: FragmentManager, private val numOfTabs: Int) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> InfoTab()
            1 -> SectionTab()
            2 -> PlaneTab()
            3 -> PreviewTab()
            else -> InfoTab()
        }
    }

    override fun getCount(): Int = numOfTabs
}
