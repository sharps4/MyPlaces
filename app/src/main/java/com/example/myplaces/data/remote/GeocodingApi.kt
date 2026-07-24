package com.example.myplaces.data.remote

import com.example.myplaces.data.remote.dto.ReverseGeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    @GET("reverse/")
    suspend fun reverse(
        @Query("lon") lon: Double,
        @Query("lat") lat: Double
    ): ReverseGeocodingResponse

    companion object {
        const val BASE_URL = "https://api-adresse.data.gouv.fr/"
    }
}