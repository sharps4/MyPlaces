package com.example.myplaces.data.local

import com.example.myplaces.domain.Place

fun PlaceEntity.toDomain() = Place(
    id = id,
    uuid = uuid,
    author = author,
    title = title,
    description = description,
    emoji = emoji,
    latitude = latitude,
    longitude = longitude,
    address = address,
    photoPath = photoPath,
    createdAt = createdAt
)

fun Place.toEntity(importedAt: Long? = null) = PlaceEntity(
    id = id,
    uuid = uuid,
    author = author,
    title = title,
    description = description,
    emoji = emoji,
    latitude = latitude,
    longitude = longitude,
    address = address,
    photoPath = photoPath,
    createdAt = createdAt,
    importedAt = importedAt
)