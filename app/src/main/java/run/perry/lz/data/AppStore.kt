package run.perry.lz.data

import android.content.Context
import run.perry.lz.BuildConfig
import run.perry.lz.data.store.Store
import run.perry.lz.data.store.asStoreProvider
import run.perry.lz.domain.http.utils.extractDomain
import run.perry.lz.utils.appContext

object AppStore {
    private val store = Store(
        appContext().getSharedPreferences("app", Context.MODE_PRIVATE)
            .asStoreProvider()
    )

    var baseurl: String by store.string(
        key = "baseurl",
        defaultValue = BuildConfig.GATEWAY_ADDRESS.extractDomain()
    )

    var musicApi: String by store.string(
        key = "music_api",
        defaultValue = ""
    )

    var splashImgUrl: String by store.string(
        key = "splash_img_url",
        defaultValue = ""
    )
    var splashActUrl: String by store.string(
        key = "splash_act_url",
        defaultValue = ""
    )

    var bannerHeight: String by store.string(
        key = "banner_height",
        defaultValue = "0.3125"
    )

    var drawerImg: String by store.string(
        key = "drawer_img",
        defaultValue = ""
    )

    var drawerTitle: String by store.string(
        key = "drawer_title",
        defaultValue = ""
    )

    var drawerInfo: String by store.string(
        key = "drawer_info",
        defaultValue = ""
    )

    var versionName: String by store.string(
        key = "latest_version_name",
        defaultValue = ""
    )

    var versionTitle: String by store.string(
        key = "latest_version_title",
        defaultValue = ""
    )

    var versionInfo: String by store.string(
        key = "latest_version_info",
        defaultValue = ""
    )

    var versionUrl: String by store.string(
        key = "latest_version_url",
        defaultValue = ""
    )

    var versionForce: Boolean by store.boolean(
        key = "latest_version_force",
        defaultValue = false
    )

    var shareQr: String by store.string(
        key = "share_qr",
        defaultValue = ""
    )

    var shareUrl: String by store.string(
        key = "share_url",
        defaultValue = ""
    )

    var playMode: Int by store.int(
        key = "play_mode",
        defaultValue = 0
    )

    var currentSongId: String by store.string(
        key = "current_song_id",
        defaultValue = ""
    )
}