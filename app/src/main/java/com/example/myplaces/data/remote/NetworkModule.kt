package com.example.myplaces.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    val geocodingApi: GeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl(GeocodingApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApi::class.java)
    }
}