package run.perry.lz.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist")
data class SongEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String?,
    val album: String?,
    val artist: String?,
    val cover: String?,
    val url: String?,
    val lrc: String?,
    val state: Int?,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createAt: Long = System.currentTimeMillis()
)