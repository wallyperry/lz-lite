package run.perry.lz.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import run.perry.lz.data.room.entity.BannerEntity

@Dao
interface BannerDao {

    @Query("SELECT * FROM banner")
    fun getAllBanner(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(bannerList: List<BannerEntity>)

    @Query("DELETE FROM banner")
    suspend fun clearBanner()
}