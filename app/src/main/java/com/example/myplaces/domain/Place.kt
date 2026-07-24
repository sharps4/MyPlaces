package com.example.myplaces.domain

data class Place(
    val id: Long,
    val uuid: String,
    val author: String?,
    val title: String,
    val description: String,
    val emoji: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val photoPath: String?,
    val createdAt: Long
) {
    val isImported: Boolean get() = author != null
    val displayAddress: String get() = address ?: "Adresse en cours de résolution…"
}