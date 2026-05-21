package com.plantdoctor.domain.model

data class SeasonalTip(
    val id: Long = 0,
    val month: Int,
    val title: String,
    val description: String,
    val iconName: String = "tips_and_updates"
)
