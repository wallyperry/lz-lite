package run.perry.lz.utils

import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LrcCache {

    private val dir = File(appContext().cacheDir, "lrc_cache")

    private fun MediaItem.filename() = "${toMusicEntity().id}.lrc"

    /**
     * 获取歌词路径
     */
    fun getLrcFilePath(music: MediaItem): String? {
        val file = File(dir, music.filename())
        return if (file.exists()) file.path else null
    }

    suspend fun saveLrcFile(music: MediaItem, content: String): File {
        return withContext(Dispatchers.IO) {
            val dir = dir.apply {
                if (!exists()) mkdirs()
            }

            File(dir, music.filename()).apply {
                writeText(content)
            }
        }
    }
}