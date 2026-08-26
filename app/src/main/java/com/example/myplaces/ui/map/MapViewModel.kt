package com.example.myplaces.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myplaces.data.repository.PlaceRepository
import com.example.myplaces.domain.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    
    private val _selectedEmojiFilter = MutableStateFlow<String?>(null)
    val selectedEmojiFilter: StateFlow<String?> = _selectedEmojiFilter

    val places: StateFlow<List<Place>> = placeRepository.observeAll()
        .combine(_selectedEmojiFilter) { places, filter ->
            if (filter == null) places
            else places.filter { it.emoji == filter }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableEmojis: StateFlow<List<String>> = placeRepository.observeAll()
        .combine(MutableStateFlow(Unit)) { places, _ -> 
            places.map { it.emoji }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFilter(emoji: String) {
        _selectedEmojiFilter.value = if (_selectedEmojiFilter.value == emoji) null else emoji
    }
}
