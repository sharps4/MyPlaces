package com.example.myplaces

import android.app.Application
import android.content.Context
import com.example.myplaces.di.AppContainer
import org.osmdroid.config.Configuration

class MyPlacesApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Configuration d'OpenStreetMap (User-Agent spécifique requis pour éviter d'être bloqué)
        val sharedPrefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        val osmConfig = Configuration.getInstance()
        osmConfig.load(this, sharedPrefs)
        // Utilisation d'un User-Agent plus "standard" et complet pour éviter le 403
        osmConfig.userAgentValue = "Mozilla/5.0 (Android; Mobile; rv:131.0) Gecko/131.0 Firefox/131.0 MyPlacesApp/${packageName}"

        container = AppContainer(this)
    }
}
