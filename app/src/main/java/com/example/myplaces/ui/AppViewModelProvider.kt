package com.example.myplaces.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myplaces.MyPlacesApp
import com.example.myplaces.ui.add.AddPlaceViewModel
import com.example.myplaces.ui.map.MapViewModel
import com.example.myplaces.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MapViewModel(myPlacesApplication().container.placeRepository)
        }
        initializer {
            AddPlaceViewModel(myPlacesApplication().container.placeRepository)
        }
        initializer {
            SettingsViewModel(
                myPlacesApplication().container.settingsRepository,
                myPlacesApplication().container.placeRepository
            )
        }
    }
}

fun CreationExtras.myPlacesApplication(): MyPlacesApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyPlacesApp)
