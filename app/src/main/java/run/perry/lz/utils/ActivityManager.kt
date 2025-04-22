package run.perry.lz.utils

import android.app.Activity
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

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
    fun finishAll() {
        for (activityRef in activities) {
            activityRef.get()?.finish()
        }
        activities.clear()
    }
}
