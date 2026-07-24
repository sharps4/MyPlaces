package com.example.myplaces.data.repository

import com.example.myplaces.domain.Place
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream

interface PlaceRepository {

    fun observeAll(): Flow<List<Place>>
    fun observeById(id: Long): Flow<Place?>

    suspend fun addPlace(
        title: String,
        description: String,
        emoji: String,
        latitude: Double,
        longitude: Double,
        photoPath: String?
    ): Long

    suspend fun deletePlace(place: Place)

    suspend fun exportToJson(author: String): File

    suspend fun importFromJson(inputStream: InputStream): ImportResult
}

data class ImportResult(
    val imported: Int,
    val skippedDuplicates: Int,
    val author: String
)