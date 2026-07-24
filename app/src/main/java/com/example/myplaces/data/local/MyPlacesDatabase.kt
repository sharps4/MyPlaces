package com.example.myplaces.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaceEntity::class], version = 1, exportSchema = false)
abstract class MyPlacesDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile private var INSTANCE: MyPlacesDatabase? = null

        fun get(context: Context): MyPlacesDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyPlacesDatabase::class.java,
                    "myplaces.db"
                ).build().also { INSTANCE = it }
            }
    }
}