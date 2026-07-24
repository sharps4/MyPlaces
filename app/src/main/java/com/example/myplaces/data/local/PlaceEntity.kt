package com.example.myplaces.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "places",
    indices = [Index(value = ["uuid"], unique = true), Index("author")]
)
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String,
    val author: String? = null,
    val title: String,
    val description: String,
    val emoji: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val photoPath: String? = null,
    val createdAt: Long,
    val importedAt: Long? = null
)