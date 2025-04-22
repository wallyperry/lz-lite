package run.perry.lz.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import run.perry.lz.data.room.entity.SongEntity

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(musicList: List<SongEntity>)

    @Query("SELECT * FROM playlist ORDER BY created_at DESC")
    fun queryAll(): List<SongEntity>

    @Query("SELECT * FROM playlist ORDER BY created_at DESC")
    fun getPlaylist(): Flow<List<SongEntity>>

    @Query("SELECT * FROM playlist WHERE id = :id")
    fun queryById(id: Int): SongEntity?

    @Delete
    fun delete(entity: SongEntity)

    @Query("DELETE FROM playlist")
    fun clear()
}