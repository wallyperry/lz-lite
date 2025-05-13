package run.perry.lz.utils

import android.app.Activity
import com.azhon.appupdate.manager.DownloadManager
import run.perry.lz.BuildConfig
import run.perry.lz.R
import run.perry.lz.data.AppStore
import kotlin.math.max

fun Activity.checkVersionUpdate(showNoUpdate: Boolean = false) {
    if (AppStore.versionCode <= BuildConfig.VERSION_CODE && showNoUpdate) {
        "当前已是最新版本".toastSuccess()
        return
    }

    DownloadManager.Builder(this)
        .apkUrl(AppStore.versionUrl)
        .apkName("${System.currentTimeMillis()}.apk")
        .apkVersionCode(AppStore.versionCode)
        .apkVersionName(AppStore.versionName)
        .apkDescription(AppStore.versionInfo.ifBlank { "最新版本：${AppStore.versionName}" })
        .forcedUpgrade(AppStore.versionForce)
        .showNewerToast(showNoUpdate)
        .showNotification(true)
        .showBgdToast(true)
        .smallIcon(R.mipmap.ic_launcher)
        .enableLog(BuildConfig.DEBUG)
        .build().download()

    //if (compareVersion(AppStore.versionName)) {
    //val uiConfig = UiConfig().apply {
    //    uiType = UiType.PLENTIFUL
    //    titleTextSize = 18f
    //}
    //val updateConfig = UpdateConfig().apply {
    //    isDebug = BuildConfig.DEBUG
    //    force = AppStore.versionForce
    //    apkSavePath = "${appContext().cacheDir.absolutePath}/apk_cache"
    //    apkSaveName = "${System.currentTimeMillis()}"
    //    isShowNotification = false
    //    notifyImgRes = R.mipmap.ic_launcher
    //    showDownloadingToast = true
    //    alwaysShowDownLoadDialog = true
    //}
    //UpdateAppUtils.getInstance().apkUrl(AppStore.versionUrl)
    //    .updateTitle(AppStore.versionTitle.ifBlank { "发现新版本" })
    //    .updateContent(AppStore.versionInfo.ifBlank { "最新版本：${AppStore.versionName}" })
    //    .updateConfig(updateConfig)
    //    .uiConfig(uiConfig)
    //    .update()
    //    return
    //}
    //if (showNoUpdate) "已是最新版本".toastSuccess()
}

/**
 * 版本比较（versionName）,不适用于类似[1.001]的格式
 */
fun compareVersion(lastVersionName: String?): Boolean {
    if (lastVersionName.isNullOrEmpty()) return false
    val curSplit: List<String> = BuildConfig.VERSION_NAME.split(".")
    val lastSplit: List<String> = lastVersionName.split(".")

    val maxLen = max(curSplit.size, lastSplit.size)
    for (i in 0 until maxLen) {
        val curNum: Int = if (i < curSplit.size) curSplit[i].toIntOrNull() ?: 0 else 0
        val lastNum: Int = if (i < lastSplit.size) lastSplit[i].toIntOrNull() ?: 0 else 0
        when {
            curNum < lastNum -> return true
            curNum > lastNum -> return false
        }
    }
    return false
}