package run.perry.lz.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album")
data class AlbumEntity(
    @PrimaryKey
    val id: Int = 0,
    val name: String?,
    val cover: String?,
    val info: String?,
    val year: String?,
    val state: Int?
)