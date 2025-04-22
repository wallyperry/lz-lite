package run.perry.lz.player

import com.danikula.videocache.HttpProxyCacheServer
import run.perry.lz.utils.appContext
import java.io.File

object ProxyCacheManager {

    private var proxy: HttpProxyCacheServer? = null

    fun getProxy(): HttpProxyCacheServer {
        if (proxy == null) {
            proxy = newProxy()
        }
        return proxy!!
    }

    private fun newProxy() = HttpProxyCacheServer.Builder(appContext())
        .cacheDirectory(File(appContext().cacheDir, "media_cache"))
        .maxCacheSize(10 * 1024 * 1024 * 1024L) // 10G
        .build()

    fun getProxyUrl(url: String?): String? = getProxy().getProxyUrl(url)

    fun isFullyCached(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        return getProxy().isCached(url)
    }
}
