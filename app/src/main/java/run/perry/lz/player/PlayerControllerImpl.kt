package run.perry.lz.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import run.perry.lz.data.AppStore
import run.perry.lz.data.room.DatabaseManager
import run.perry.lz.utils.toMediaItem
import run.perry.lz.utils.toSongEntity
import run.perry.lz.utils.toastError

class PlayerControllerImpl(
    private val player: Player
) : PlayerController, CoroutineScope by MainScope() {

    private val store by lazy { AppStore }
    private val dao by lazy { DatabaseManager.getDatabase().playlistDao() }

    private val _playlist = MutableLiveData(emptyList<MediaItem>())
    override val playlist: LiveData<List<MediaItem>> = _playlist

    private val _currentSong = MutableLiveData<MediaItem?>(null)
    override val currentSong: LiveData<MediaItem?> = _currentSong

    private val _playState = MutableStateFlow<PlayState>(PlayState.Idle)
    override val playState: StateFlow<PlayState> = _playState

    private val _songDuration = MutableStateFlow<Long>(0)
    override val songDuration: StateFlow<Long> = _songDuration

    private val _playProgress = MutableStateFlow<Long>(0)
    override val playProgress: StateFlow<Long> = _playProgress

    private val _bufferingPercent = MutableStateFlow(0)
    override val bufferingPercent: StateFlow<Int> = _bufferingPercent

    private val _bufferedPosition = MutableStateFlow(0L)
    override val bufferedPosition: StateFlow<Long> = _bufferedPosition

    private val _playMode = MutableStateFlow(PlayMode.valueOf(store.playMode))
    override val playMode: StateFlow<PlayMode> = _playMode
    private var audioSessionId = 0

    init {
        player.playWhenReady = false
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        _playState.value = PlayState.Idle
                        _songDuration.value = 0
                        _playProgress.value = 0
                        _bufferingPercent.value = 0
                        _bufferedPosition.value = 0
                    }

                    Player.STATE_BUFFERING -> {
                        _playState.value = PlayState.Preparing
                    }

                    Player.STATE_READY -> {
                        player.play()
                        _playState.value = PlayState.Playing
                        player.duration.also { if (it != C.TIME_UNSET) _songDuration.value = it }
                    }

                    Player.STATE_ENDED -> {
                        _playState.value = PlayState.Idle

                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (player.playbackState == Player.STATE_READY) {
                    _playState.value = if (isPlaying) PlayState.Playing else PlayState.Pause
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                mediaItem ?: return
                val playlist = _playlist.value ?: return
                _currentSong.value = playlist.find { it.mediaId == mediaItem.mediaId }
                _songDuration.value = mediaItem.mediaMetadata.durationMs ?: 0
            }

            @androidx.annotation.OptIn(UnstableApi::class)
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                this@PlayerControllerImpl.audioSessionId = audioSessionId
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                "播放失败(${error.errorCodeName},${error.localizedMessage})".toastError()
                next()
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                super.onTimelineChanged(timeline, reason)
                if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                    val window = Timeline.Window()
                    timeline.getWindow(0, window)
                    window.durationMs.also { if (it != C.TIME_UNSET) _songDuration.value = it }
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    player.duration.also { if (it != C.TIME_UNSET) _songDuration.value = it }
                }
            }
        })

        setPlayMode(PlayMode.valueOf(store.playMode))

        launch(Dispatchers.Main.immediate) {
            val playlist: List<MediaItem> = withContext(Dispatchers.IO) {
                dao.queryAll().map { it.toMediaItem() }
            }
            if (playlist.isNotEmpty()) {
                _playlist.value = playlist
                player.setMediaItems(playlist)
                val currentSongId = store.currentSongId
                if (currentSongId.isNotEmpty()) {
                    val currentSongIndex = playlist.indexOfFirst {
                        it.mediaId == currentSongId
                    }.coerceAtLeast(0)
                    _currentSong.value = playlist[currentSongIndex]
                    player.seekTo(currentSongIndex, 0)
                }
            }

            _currentSong.observeForever {
                store.currentSongId = it?.mediaId.orEmpty()
            }
        }

        launch {
            while (true) {
                if (player.isPlaying) {
                    _playProgress.value = player.currentPosition
                    _bufferingPercent.value = player.bufferedPercentage
                    _bufferedPosition.value = player.bufferedPosition
                    player.bufferedPosition
                    player.duration.also { if (it != C.TIME_UNSET) _songDuration.value = it }
                }
                delay(1000)
            }
        }
    }

    override fun add(song: MediaItem) {
        launch(Dispatchers.Main.immediate) {
            val newPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
            val isEmptyPlaylist = newPlaylist.isEmpty()
            val index = newPlaylist.indexOfFirst { it.mediaId == song.mediaId }
            if (index >= 0) {
                newPlaylist[index] = song
                player.replaceMediaItem(index, song)
            } else {
                newPlaylist.add(song)
                player.addMediaItem(song)
            }
            withContext(Dispatchers.IO) {
                dao.clear()
                newPlaylist.reversed().map { dao.insert(it.toSongEntity()) }
            }
            _playlist.value = newPlaylist
            if (isEmptyPlaylist) _currentSong.value = newPlaylist[0]
        }
    }

    override fun addAndPlay(song: MediaItem) {
        launch(Dispatchers.Main.immediate) {
            val newPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
            val index = newPlaylist.indexOfFirst { it.mediaId == song.mediaId }
            if (index >= 0) {
                newPlaylist[index] = song
                player.replaceMediaItem(index, song)
            } else {
                newPlaylist.add(song)
                player.addMediaItem(song)
            }
            withContext(Dispatchers.IO) {
                dao.clear()
                newPlaylist.reversed().map { dao.insert(it.toSongEntity()) }
            }
            _playlist.value = newPlaylist
            when {
                _currentSong.value?.mediaId != song.mediaId -> play(song.mediaId)
                _playState.value != PlayState.Playing -> play(song.mediaId)
            }
        }
    }

    //override fun addAll(songList: List<MediaItem>) {
    //    launch(Dispatchers.Main.immediate) {
    //        val newPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
    //        val isEmptyPlaylist = newPlaylist.isEmpty()
    //        songList.map { song ->
    //            val index = newPlaylist.indexOfFirst { it.mediaId == song.mediaId }
    //            if (index >= 0) {
    //                newPlaylist[index] = song
    //                player.replaceMediaItem(index, song)
    //            } else {
    //                newPlaylist.add(song)
    //                player.addMediaItem(song)
    //            }
    //        }
    //        withContext(Dispatchers.IO) {
    //            dao.clear()
    //            newPlaylist.reversed().map { dao.insert(it.toSongEntity()) }
    //        }
    //        _playlist.value = newPlaylist
    //        if (isEmptyPlaylist) _currentSong.value = newPlaylist[0]
    //    }
    //}

    override fun addAll(songList: List<MediaItem>) {
        launch {
            val existingPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
            val existingMediaIds = existingPlaylist.map { it.mediaId }.toSet()
            val newMediaItems = mutableListOf<MediaItem>()

            // 构建新的播放列表 & 去重合并逻辑（全部在后台做）
            val finalPlaylist = existingPlaylist.toMutableList()
            songList.forEach { song ->
                if (existingMediaIds.contains(song.mediaId)) {
                    val index = finalPlaylist.indexOfFirst { it.mediaId == song.mediaId }
                    if (index >= 0) finalPlaylist[index] = song
                } else {
                    finalPlaylist.add(song)
                    newMediaItems.add(song)
                }
            }

            // 数据库操作移到 IO 线程
            withContext(Dispatchers.IO) {
                dao.clear()
                dao.insertAll(finalPlaylist.reversed().map { it.toSongEntity() }) // 批量插入（需你实现）
            }

            withContext(Dispatchers.Main) {
                // 播放器更新：仅添加新的项（或也可以 replace 全部）
                newMediaItems.forEach { player.addMediaItem(it) }
                _playlist.value = finalPlaylist
                if (existingPlaylist.isEmpty() && finalPlaylist.isNotEmpty()) {
                    _currentSong.value = finalPlaylist[0]
                }
            }
        }
    }


    override fun replaceAll(songList: List<MediaItem>, song: MediaItem) {
        launch(Dispatchers.Main.immediate) {
            withContext(Dispatchers.IO) {
                dao.clear()
                songList.reversed().forEach { dao.insert(it.toSongEntity()) }
            }

            val currentList = _playlist.value.orEmpty()
            val isSameList = currentList.map { it.mediaId } == songList.map { it.mediaId }
            val currentId = player.currentMediaItem?.mediaId

            if (!isSameList) {
                val indexToPlay = songList.indexOfFirst { it.mediaId == song.mediaId }.coerceAtLeast(0)
                player.setMediaItems(songList, indexToPlay, C.TIME_UNSET)
                player.prepare()
            } else if (currentId != song.mediaId) {
                // 仅跳曲，不重建 MediaSource
                val indexToPlay = songList.indexOfFirst { it.mediaId == song.mediaId }.coerceAtLeast(0)
                player.seekTo(indexToPlay, C.TIME_UNSET)
            }

            _playlist.value = songList
            _currentSong.value = song
            if (_playState.value != PlayState.Playing) play(song.mediaId)
        }
    }


    override fun play(mediaId: String) {
        if (_currentSong.value?.mediaId == mediaId && _playState.value == PlayState.Playing) {
            return
        }
        val playlist = _playlist.value
        if (playlist.isNullOrEmpty()) {
            return
        }
        val index = playlist.indexOfFirst { it.mediaId == mediaId }
        if (index < 0) {
            return
        }

        stop()
        player.seekTo(index, 0)
        player.prepare()

        _currentSong.value = playlist[index]
        _songDuration.value = 0
        _playProgress.value = 0
        _bufferingPercent.value = 0
        _bufferedPosition.value = 0
    }

    override fun delete(song: MediaItem) {
        launch(Dispatchers.Main.immediate) {
            val playlist = _playlist.value?.toMutableList() ?: mutableListOf()
            val index = playlist.indexOfFirst { it.mediaId == song.mediaId }
            if (index < 0) return@launch
            if (playlist.size == 1) {
                clearPlaylist()
            } else {
                playlist.removeAt(index)
                _playlist.value = playlist
                withContext(Dispatchers.IO) {
                    dao.delete(song.toSongEntity())
                }
                player.removeMediaItem(index)
            }
        }
    }

    override fun clearPlaylist() {
        launch(Dispatchers.Main.immediate) {
            withContext(Dispatchers.IO) {
                dao.clear()
            }
            stop()
            player.clearMediaItems()
            _playlist.value = emptyList()
            _currentSong.value = null
        }
    }

    override fun playPause() {
        if (player.mediaItemCount == 0) return
        when (player.playbackState) {
            Player.STATE_IDLE -> {
                player.prepare()
            }

            Player.STATE_BUFFERING -> {
                stop()
            }

            Player.STATE_READY -> {
                if (player.isPlaying) {
                    player.pause()
                    _playState.value = PlayState.Pause
                } else {
                    player.play()
                    _playState.value = PlayState.Playing
                }
            }

            Player.STATE_ENDED -> {
                player.seekToNextMediaItem()
                player.prepare()
            }
        }
    }

    override fun next() {
        if (player.mediaItemCount == 0) return
        player.seekToNextMediaItem()
        player.prepare()
        _songDuration.value = 0
        _playProgress.value = 0
        _bufferingPercent.value = 0
        _bufferedPosition.value = 0
    }

    override fun prev() {
        if (player.mediaItemCount == 0) return
        player.seekToPreviousMediaItem()
        player.prepare()
        _songDuration.value = 0
        _playProgress.value = 0
        _bufferingPercent.value = 0
        _bufferedPosition.value = 0
    }

    override fun seekTo(msec: Int) {
        if (player.playbackState == Player.STATE_READY) {
            player.seekTo(msec.toLong())
        }
    }

    override fun getAudioSessionId(): Int {
        return audioSessionId
    }

    override fun setPlayMode(mode: PlayMode) {
        store.playMode = mode.value
        _playMode.value = mode
        when (mode) {
            PlayMode.Loop -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = false
            }

            PlayMode.Shuffle -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = true
            }

            PlayMode.Single -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.shuffleModeEnabled = false
            }
        }
    }

    override fun stop() {
        player.stop()
        _playState.value = PlayState.Idle
    }

    override fun isPlaying(songId: String?): Boolean {
        return _currentSong.value?.mediaId == songId
    }
}