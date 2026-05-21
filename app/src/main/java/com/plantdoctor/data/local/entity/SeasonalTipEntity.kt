package com.plantdoctor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seasonal_tips")
data class SeasonalTipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: Int, // 1-12
    val title: String,
    val description: String,
    val iconName: String = "tips_and_updates"
)
