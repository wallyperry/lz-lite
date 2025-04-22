package run.perry.lz.data.room.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import run.perry.lz.player.PlayerManager

@Entity(tableName = "music")
data class MusicEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String?,
    val album: String?,
    val artist: String?,
    val cover: String?,
    val url: String?,
    val lrc: String?,
    val state: Int?,

    @Ignore
    var isCached: Boolean? = null   // null=未知，true=已缓存，false=未缓存
) {

    constructor(
        id: Int,
        name: String?,
        album: String?,
        artist: String?,
        cover: String?,
        url: String?,
        lrc: String?,
        state: Int?
    ) : this(id, name, album, artist, cover, url, lrc, state, null)

    @get:Ignore
    val playing: Boolean
        get() = PlayerManager.getController().isPlaying("$id")
}