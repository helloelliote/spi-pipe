package kr.djspi.pipe01.tab

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(activity: AppCompatActivity, private val tabList: List<Fragment>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = tabList.size

    override fun createFragment(position: Int): Fragment = tabList[position]
}