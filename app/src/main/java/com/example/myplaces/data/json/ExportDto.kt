package com.example.myplaces.data.json

data class ExportFile(
    val formatVersion: Int = 1,
    val exportedAt: Long,
    val author: String,
    val places: List<PlaceJson>
)

data class PlaceJson(
    val uuid: String,
    val title: String,
    val description: String,
    val emoji: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val createdAt: Long,
    val photoBase64: String? = null
)