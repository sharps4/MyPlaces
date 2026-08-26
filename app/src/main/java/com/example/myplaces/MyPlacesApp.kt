package com.example.myplaces

import android.app.Application
import com.example.myplaces.di.AppContainer

class MyPlacesApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
