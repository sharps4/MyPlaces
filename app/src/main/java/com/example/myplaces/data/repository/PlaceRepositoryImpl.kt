package com.example.myplaces.data.repository

import android.content.Context
import com.example.myplaces.data.json.ExportFile
import com.example.myplaces.data.json.PlaceJson
import com.example.myplaces.data.local.PlaceDao
import com.example.myplaces.data.local.PlaceEntity
import com.example.myplaces.data.local.toDomain
import com.example.myplaces.data.local.toEntity
import com.example.myplaces.data.remote.GeocodingDataSource
import com.example.myplaces.domain.Place
import com.example.myplaces.util.PhotoStorage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

class PlaceRepositoryImpl(
    context: Context,
    private val dao: PlaceDao,
    private val geocoding: GeocodingDataSource,
    private val externalScope: CoroutineScope,
    private val gson: Gson = Gson()
) : PlaceRepository {

    private val appContext = context.applicationContext

    override fun observeAll(): Flow<List<Place>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeMine(): Flow<List<Place>> =
        dao.observeMine().map { list -> list.map { it.toDomain() } }

    override fun observeImported(): Flow<List<Place>> =
        dao.observeImported().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Place?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: Long): Place? = dao.getById(id)?.toDomain()

    override suspend fun addPlace(
        title: String,
        description: String,
        emoji: String,
        latitude: Double,
        longitude: Double,
        photoPath: String?
    ): Long {
        val entity = PlaceEntity(
            uuid = UUID.randomUUID().toString(),
            author = null, // null => lieu de l'utilisateur local
            title = title.trim(),
            description = description.trim(),
            emoji = emoji,
            latitude = latitude,
            longitude = longitude,
            address = null, // résolue en arrière-plan juste après
            photoPath = photoPath,
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insert(entity)
        externalScope.launch { resolveAddress(id) }
        return id
    }

    override suspend fun updatePlace(place: Place) {
        val existing = dao.getById(place.id) ?: return
        dao.update(place.toEntity(importedAt = existing.importedAt))
    }

    override suspend fun deletePlace(place: Place) {
        PhotoStorage.delete(place.photoPath)
        dao.delete(place.toEntity())
    }

    override suspend fun resolveAddress(id: Long): String? {
        val entity = dao.getById(id) ?: return null
        val address = geocoding.reverseGeocode(entity.latitude, entity.longitude) ?: return null
        dao.updateAddress(id, address)
        return address
    }

    override suspend fun exportToJson(author: String): File = withContext(Dispatchers.IO) {
        val places = dao.getAllOnce().map { entity ->
            PlaceJson(
                uuid = entity.uuid,
                title = entity.title,
                description = entity.description,
                emoji = entity.emoji,
                latitude = entity.latitude,
                longitude = entity.longitude,
                address = entity.address,
                createdAt = entity.createdAt,
                photoBase64 = PhotoStorage.encodeToBase64(entity.photoPath)
            )
        }
        val payload = ExportFile(
            exportedAt = System.currentTimeMillis(),
            author = author.trim().ifEmpty { "anonyme" },
            places = places
        )
        File(PhotoStorage.exportsDir(appContext), ExportFile.FILE_NAME).apply {
            writer(Charsets.UTF_8).use { gson.toJson(payload, it) }
        }
    }

    override suspend fun importFromJson(inputStream: InputStream): ImportResult =
        withContext(Dispatchers.IO) {
            val payload = try {
                InputStreamReader(inputStream, Charsets.UTF_8).use {
                    gson.fromJson(it, ExportFile::class.java)
                }
            } catch (e: JsonSyntaxException) {
                throw InvalidExportFileException("Fichier JSON illisible : ${e.message}")
            } ?: throw InvalidExportFileException("Fichier vide")

            if (payload.formatVersion > ExportFile.FORMAT_VERSION) {
                throw InvalidExportFileException(
                    "Format v${payload.formatVersion} non supporté (version max : ${ExportFile.FORMAT_VERSION})"
                )
            }

            val author = payload.author.trim().ifEmpty { "anonyme" }
            val knownUuids = dao.getAllUuids().toSet()

            // On filtre AVANT de matérialiser les photos, sinon un doublon laisserait
            // un fichier orphelin dans le stockage interne.
            val candidates = payload.places.filter { it.uuid.isNotBlank() }
            val newOnes = candidates.filterNot { it.uuid in knownUuids }
            val now = System.currentTimeMillis()

            val entities = newOnes.map { json ->
                PlaceEntity(
                    uuid = json.uuid,
                    author = author, // non-null => lieu importé, distinct des miens
                    title = json.title,
                    description = json.description,
                    emoji = json.emoji,
                    latitude = json.latitude,
                    longitude = json.longitude,
                    address = json.address,
                    photoPath = PhotoStorage.decodeFromBase64(appContext, json.photoBase64),
                    createdAt = json.createdAt,
                    importedAt = now
                )
            }

            val insertedIds = dao.insertAllIgnoringDuplicates(entities)
            val inserted = insertedIds.count { it != -1L }

            // Une insertion refusée (doublon détecté entre-temps) ne doit pas laisser
            // sa photo derrière elle.
            insertedIds.forEachIndexed { index, rowId ->
                if (rowId == -1L) PhotoStorage.delete(entities[index].photoPath)
            }

            ImportResult(
                imported = inserted,
                skippedDuplicates = candidates.size - inserted,
                author = author
            )
        }
}
