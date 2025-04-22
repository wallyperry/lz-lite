package run.perry.lz.utils

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import run.perry.lz.data.room.entity.MusicEntity
import run.perry.lz.data.room.entity.SongEntity
import run.perry.lz.player.ProxyCacheManager

@OptIn(UnstableApi::class)
fun MusicEntity.toMediaItem() = MediaItem.Builder()
    .setMediaId("$id")
    .setUri(ProxyCacheManager.getProxyUrl(url))
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(name)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setAlbumArtist(artist)
            .setArtworkUri(cover?.toUri())
            .setCover(cover.orEmpty())
            .setUrl(url.orEmpty())
            .setLrc(lrc.orEmpty())
            .setState(state)
            .build()
    )
    .build()

fun MediaItem.toMusicEntity() = MusicEntity(
    id = mediaId.toInt(),
    name = mediaMetadata.title?.toString(),
    album = mediaMetadata.albumTitle?.toString(),
    artist = mediaMetadata.artist?.toString(),
    cover = mediaMetadata.getCover(),
    url = mediaMetadata.getUrl(),
    lrc = mediaMetadata.getLrc(),
    state = mediaMetadata.getState()
)

fun List<MusicEntity>.toMediaItems(): MutableList<MediaItem> = mapTo(ArrayList()) { it.toMediaItem() }

@OptIn(UnstableApi::class)
fun SongEntity.toMediaItem() = MediaItem.Builder()
    .setMediaId("$id")
    .setUri(ProxyCacheManager.getProxyUrl(url))
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(name)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setAlbumArtist(artist)
            .setArtworkUri(cover?.toUri())
            .setCover(cover.orEmpty())
            .setUrl(url.orEmpty())
            .setLrc(lrc.orEmpty())
            .setState(state)
            .build()
    )
    .build()

fun List<SongEntity>.toMusicEntities(): MutableList<MusicEntity> = mapTo(ArrayList()) { it.toMusicEntity() }

fun SongEntity.toMusicEntity() = MusicEntity(
    id = id,
    name = name,
    album = album,
    artist = artist,
    cover = cover,
    url = url,
    lrc = lrc,
    state = state
)

fun MediaItem.toSongEntity() = SongEntity(
    id = mediaId.toInt(),
    name = mediaMetadata.title?.toString(),
    album = mediaMetadata.albumTitle?.toString(),
    artist = mediaMetadata.artist?.toString(),
    cover = mediaMetadata.getCover(),
    url = mediaMetadata.getUrl(),
    lrc = mediaMetadata.getLrc(),
    state = mediaMetadata.getState()
)

const val EXTRA_COVER = "extra_cover"
const val EXTRA_URL = "extra_url"
const val EXTRA_LRC = "extra_lrc"
const val EXTRA_STATE = "extra_state"

fun MediaMetadata.getCover() = extras?.getString(EXTRA_COVER)
fun MediaMetadata.Builder.setCover(value: String) = apply {
    setExtras((build().extras ?: bundleOf()).apply {
        putString(EXTRA_COVER, value)
    })
}

fun MediaMetadata.getUrl() = extras?.getString(EXTRA_URL)
fun MediaMetadata.Builder.setUrl(value: String) = apply {
    setExtras((build().extras ?: bundleOf()).apply {
        putString(EXTRA_URL, value)
    })
}

fun MediaMetadata.getLrc() = extras?.getString(EXTRA_LRC)
fun MediaMetadata.Builder.setLrc(value: String) = apply {
    setExtras((build().extras ?: bundleOf()).apply {
        putString(EXTRA_LRC, value)
    })
}

fun MediaMetadata.getState() = extras?.getInt(EXTRA_STATE)
fun MediaMetadata.Builder.setState(value: Int?) = apply {
    setExtras((build().extras ?: bundleOf()).apply {
        putInt(EXTRA_STATE, value ?: 0)
    })
}

fun Long.toFormattedDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}