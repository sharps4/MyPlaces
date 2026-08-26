package com.example.myplaces.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeocodingDataSource(
    private val api: GeocodingApi = NetworkModule.geocodingApi
) {

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            runCatching { api.reverse(lon = longitude, lat = latitude).firstLabel }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
        }
}
