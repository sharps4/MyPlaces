package com.example.myplaces.di

import android.content.Context
import com.example.myplaces.data.local.MyPlacesDatabase
import com.example.myplaces.data.remote.GeocodingDataSource
import com.example.myplaces.data.repository.PlaceRepository
import com.example.myplaces.data.repository.PlaceRepositoryImpl
import com.example.myplaces.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database: MyPlacesDatabase by lazy { MyPlacesDatabase.get(appContext) }

    private val geocodingDataSource: GeocodingDataSource by lazy { GeocodingDataSource() }

    val placeRepository: PlaceRepository by lazy {
        PlaceRepositoryImpl(
            context = appContext,
            dao = database.placeDao(),
            geocoding = geocodingDataSource,
            externalScope = applicationScope
        )
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }
}
