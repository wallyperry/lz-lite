package run.perry.lz.utils

import android.app.Activity
import android.content.Intent
import com.umeng.analytics.MobclickAgent
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.exitProcess

object ActivityManager {
    private val activities = CopyOnWriteArrayList<WeakReference<Activity>>()  // 存储 Activity


    /** 添加 Activity */
    fun addActivity(activity: Activity) {
        activities.add(WeakReference(activity))
    }

    /** 移除 Activity */
    fun removeActivity(activity: Activity) {
        activities.removeAll { it.get() == activity || it.get() == null }
    }

    /** 结束所有 Activity */
    private fun finishAll() {
        for (activityRef in activities) {
            activityRef.get()?.finish()
        }
        activities.clear()
    }

    /** 退出应用程序 */
    fun exit(activity: Activity) {
        try {
            MobclickAgent.onKillProcess(activity)
            activity.startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finishAll()
            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
