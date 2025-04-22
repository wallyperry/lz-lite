package run.perry.lz.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * @author Perry
 * @date 2022/5/21
 * TG https://t.me/wallyperry
 */
class BaseFragmentStateAdapter(
    activity: FragmentActivity,
    private val fragments: List<Fragment>,
    private val titles: List<String>? = null
) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment = fragments[position]
    override fun getItemCount(): Int = fragments.size
    fun getPageFragment(position: Int): Fragment = fragments[position]
    fun getPageTitle(position: Int): String? = titles?.let { it[position] }
}