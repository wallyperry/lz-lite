package run.perry.lz.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import run.perry.lz.data.room.entity.MusicEntity

@Dao
interface MusicDao {

    @Query("SELECT * FROM music")
    suspend fun getAllMusic(): List<MusicEntity>

    @Query("SELECT * FROM music WHERE album=:album")
    suspend fun getMusicListByAlbum(album: String): List<MusicEntity>

    @Query("SELECT * FROM music WHERE album=:album")
    fun getMusicsByAlbum(album: String): Flow<List<MusicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(musicList: List<MusicEntity>)

    @Query(
        """
        SELECT * FROM music 
        WHERE name LIKE '%' || :keyword || '%' 
           OR album LIKE '%' || :keyword || '%' 
           OR artist LIKE '%' || :keyword || '%'
    """
    )
    fun searchMusic(keyword: String): Flow<List<MusicEntity>>

    @Query("DELETE FROM music")
    suspend fun clearMusic()

}