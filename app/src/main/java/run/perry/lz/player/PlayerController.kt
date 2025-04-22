package run.perry.lz.player

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val playlist: LiveData<List<MediaItem>>
    val currentSong: LiveData<MediaItem?>
    val playState: StateFlow<PlayState>
    val songDuration: StateFlow<Long>
    val playProgress: StateFlow<Long>
    val bufferingPercent: StateFlow<Int>
    val bufferedPosition: StateFlow<Long>
    val playMode: StateFlow<PlayMode>

    @MainThread
    fun add(song: MediaItem)

    @MainThread
    fun addAndPlay(song: MediaItem)

    @MainThread
    fun addAll(songList: List<MediaItem>)

    @MainThread
    fun replaceAll(songList: List<MediaItem>, song: MediaItem)

    @MainThread
    fun play(mediaId: String)

    @MainThread
    fun delete(song: MediaItem)

    @MainThread
    fun clearPlaylist()

    @MainThread
    fun playPause()

    @MainThread
    fun next()

    @MainThread
    fun prev()

    @MainThread
    fun seekTo(msec: Int)

    @MainThread
    fun getAudioSessionId(): Int

    @MainThread
    fun setPlayMode(mode: PlayMode)

    @MainThread
    fun stop()

    @MainThread
    fun isPlaying(songId: String?): Boolean
}