package com.example.myplaces.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE author IS NULL ORDER BY createdAt DESC")
    fun observeMine(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE author IS NOT NULL ORDER BY createdAt DESC")
    fun observeImported(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    fun observeById(id: Long): Flow<PlaceEntity?>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getById(id: Long): PlaceEntity?

    @Query("SELECT * FROM places")
    suspend fun getAllOnce(): List<PlaceEntity>

    @Query("SELECT uuid FROM places")
    suspend fun getAllUuids(): List<String>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(place: PlaceEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringDuplicates(places: List<PlaceEntity>): List<Long>

    @Update
    suspend fun update(place: PlaceEntity)

    @Query("UPDATE places SET address = :address WHERE id = :id")
    suspend fun updateAddress(id: Long, address: String?)

    @Delete
    suspend fun delete(place: PlaceEntity)
}