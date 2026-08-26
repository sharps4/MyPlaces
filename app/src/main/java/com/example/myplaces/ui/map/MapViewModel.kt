package com.example.myplaces.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myplaces.data.repository.PlaceRepository
import com.example.myplaces.domain.Place
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    val places: StateFlow<List<Place>> = placeRepository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
