package com.example.myplaces.data.json

data class ExportFile(
    val formatVersion: Int = FORMAT_VERSION,
    val exportedAt: Long = 0L,
    val author: String = "",
    val places: List<PlaceJson> = emptyList()
) {
    companion object {
        const val FORMAT_VERSION = 1
        const val FILE_NAME = "places_export.json"
    }
}

data class PlaceJson(
    val uuid: String = "",
    val title: String = "",
    val description: String = "",
    val emoji: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null,
    val createdAt: Long = 0L,
    val photoBase64: String? = null
)
