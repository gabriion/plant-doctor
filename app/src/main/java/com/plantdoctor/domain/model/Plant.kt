package com.plantdoctor.domain.model

data class Plant(
    val id: Long = 0,
    val name: String,
    val species: String = "",
    val location: String = "",
    val photoUri: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
