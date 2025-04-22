package run.perry.lz.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import run.perry.lz.data.room.entity.AlbumEntity

@Dao
interface AlbumDao {

    @Query(
        """
    SELECT * FROM album
    ORDER BY 
        CASE 
            WHEN year IS NULL OR TRIM(year) = '' THEN 1
            ELSE 0
        END,
        year ASC
"""
    )
    fun getAllAlbumsSortedByYear(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(albumList: List<AlbumEntity>)

    @Query("DELETE FROM album")
    suspend fun clearAlbum()
}