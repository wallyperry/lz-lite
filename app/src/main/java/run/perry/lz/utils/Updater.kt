package run.perry.lz.utils

import android.app.Activity
import com.azhon.appupdate.manager.DownloadManager
import run.perry.lz.BuildConfig
import run.perry.lz.R
import run.perry.lz.data.AppStore

fun Activity.checkVersionUpdate(showNoUpdate: Boolean = false) {
    DownloadManager.Builder(this).run {
        apkUrl(AppStore.versionUrl)
        apkName("${System.currentTimeMillis()}.apk")
        smallIcon(R.mipmap.ic_launcher)
        showNewerToast(showNoUpdate)
        apkVersionCode(AppStore.versionCode)
        apkVersionName(AppStore.versionName)
        apkDescription(AppStore.versionInfo)
        enableLog(BuildConfig.DEBUG)
        showNotification(false)
        showBgdToast(false)
        forcedUpgrade(AppStore.versionForce)
        build()
    }.download()
}