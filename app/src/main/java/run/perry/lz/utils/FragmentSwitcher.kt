package run.perry.lz.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import kotlin.reflect.KClass

class FragmentSwitcher(
    private val activity: FragmentActivity,
    private val containerId: Int
) {

    private var currentFragment: Fragment? = null
    private val fragmentCache = mutableMapOf<String, Fragment>()

    /**
     * 切换到指定的 Fragment 类
     */
    fun <T : Fragment> switchTo(fragmentClass: KClass<T>) {
        val tag = fragmentClass.simpleName ?: return
        val fragmentManager = activity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // 淡入淡出动画
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        // 如果当前已经是该 Fragment，跳过
        if (currentFragment?.tag == tag) return

        // 尝试从 FragmentManager 或缓存中获取
        val targetFragment = fragmentManager.findFragmentByTag(tag)
            ?: fragmentCache.getOrPut(tag) {
                fragmentClass.java.getDeclaredConstructor().newInstance()
            }

        // 隐藏当前 Fragment
        currentFragment?.let {
            transaction.hide(it)
        }

        // 显示或添加目标 Fragment
        if (!targetFragment.isAdded) {
            transaction.add(containerId, targetFragment, tag)
        } else {
            transaction.show(targetFragment)
        }

        currentFragment = targetFragment
        transaction.commit()
    }

    /**
     * 也可以支持直接传入 Fragment 实例（可选扩展）
     */
    fun switchTo(fragment: Fragment) {
        val tag = fragment::class.java.simpleName
        val fragmentManager = activity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // 淡入淡出动画
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if (currentFragment?.tag == tag) return

        currentFragment?.let { transaction.hide(it) }

        if (!fragment.isAdded) {
            transaction.add(containerId, fragment, tag)
        } else {
            transaction.show(fragment)
        }

        currentFragment = fragment
        transaction.commit()
    }
}
