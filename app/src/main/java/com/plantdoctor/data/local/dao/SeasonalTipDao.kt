package com.plantdoctor.data.local.dao

import androidx.room.*
import com.plantdoctor.data.local.entity.SeasonalTipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonalTipDao {
    @Query("SELECT * FROM seasonal_tips WHERE month = :month")
    fun getTipsForMonth(month: Int): Flow<List<SeasonalTipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<SeasonalTipEntity>)

    @Query("SELECT COUNT(*) FROM seasonal_tips")
    suspend fun getTipCount(): Int
}
