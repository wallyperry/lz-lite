package run.perry.lz.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import run.perry.lz.data.room.dao.AlbumDao
import run.perry.lz.data.room.dao.BannerDao
import run.perry.lz.data.room.dao.MusicDao
import run.perry.lz.data.room.dao.PlaylistDao
import run.perry.lz.data.room.entity.AlbumEntity
import run.perry.lz.data.room.entity.BannerEntity
import run.perry.lz.data.room.entity.MusicEntity
import run.perry.lz.data.room.entity.SongEntity

@Database(
    entities = [
        AlbumEntity::class,
        MusicEntity::class,
        BannerEntity::class,
        SongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LzDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun musicDao(): MusicDao
    abstract fun bannerDao(): BannerDao
    abstract fun playlistDao(): PlaylistDao
}