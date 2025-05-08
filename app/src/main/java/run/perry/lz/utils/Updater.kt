package run.perry.lz.utils

import android.app.Activity
import constant.UiType
import model.UiConfig
import model.UpdateConfig
import run.perry.lz.BuildConfig
import run.perry.lz.R
import run.perry.lz.data.AppStore
import update.UpdateAppUtils
import kotlin.math.max

fun Activity.checkVersionUpdate(showNoUpdate: Boolean = false) {
    if (compareVersion(AppStore.versionName)) {
        val uiConfig = UiConfig().apply {
            uiType = UiType.PLENTIFUL
            titleTextSize = 18f
        }
        val updateConfig = UpdateConfig().apply {
            isDebug = BuildConfig.DEBUG
            force = AppStore.versionForce
            apkSavePath = "${appContext().cacheDir.absolutePath}/apk_cache"
            apkSaveName = "${System.currentTimeMillis()}"
            isShowNotification = false
            notifyImgRes = R.mipmap.ic_launcher
            showDownloadingToast = true
            alwaysShowDownLoadDialog = true
        }
        UpdateAppUtils.getInstance().apkUrl(AppStore.versionUrl)
            .updateTitle(AppStore.versionTitle.ifBlank { "发现新版本" })
            .updateContent(AppStore.versionInfo.ifBlank { "最新版本：${AppStore.versionName}" })
            .updateConfig(updateConfig)
            .uiConfig(uiConfig)
            .update()
        return
    }
    if (showNoUpdate) "已是最新版本".toastSuccess()
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