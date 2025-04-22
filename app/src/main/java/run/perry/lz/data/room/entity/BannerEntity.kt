package run.perry.lz.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banner")
data class BannerEntity(
    @PrimaryKey
    val id: Int = 0,
    val title: String?,
    val img: String?,
    val url: String?
)