package run.perry.lz.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import run.perry.lz.utils.appContext
import java.io.File

@UnstableApi
object MediaCacheManager {

    private const val MAX_CACHE_SIZE = 20 * 1024L * 1024L * 1024L // 20G
    private var simpleCache: SimpleCache? = null

    fun getCache(): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(appContext().cacheDir, "media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
            val dbProvider = StandaloneDatabaseProvider(appContext())
            simpleCache = SimpleCache(cacheDir, evictor, dbProvider)
        }
        return simpleCache!!
    }

    fun clearCache() {
        try {
            getCache().release() // 必须先释放
            val cacheDir = File(appContext().cacheDir, "media_cache")
            cacheDir.deleteRecursively()
            simpleCache = null // 重置引用
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isFullyCached(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        val cache = getCache()
        val cachedSpans = cache.getCachedSpans(url).toList()
        if (cachedSpans.isEmpty()) return false

        // 估算内容长度：最大位置 + 对应的长度
        val contentEnd = cachedSpans.maxOf { it.position + it.length }

        // 构建一个 bitSet，标记每个字节是否被缓存（以 1KB 为粒度优化效率）
        val pieceSize = 1024L
        val totalPieces = (contentEnd / pieceSize + 1).toInt()
        val cachedPieces = BooleanArray(totalPieces)

        for (span in cachedSpans) {
            var start = span.position / pieceSize
            val end = (span.position + span.length) / pieceSize
            for (i in start..end) {
                if (i in cachedPieces.indices) {
                    cachedPieces[i.toInt()] = true
                }
            }
        }

        // 检查是否全部缓存
        return cachedPieces.all { it }
    }
}