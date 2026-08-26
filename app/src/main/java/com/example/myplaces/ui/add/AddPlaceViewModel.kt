package com.example.myplaces.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myplaces.data.repository.PlaceRepository
import kotlinx.coroutines.launch

class AddPlaceViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var emoji by mutableStateOf("📍")
    var photoPath by mutableStateOf<String?>(null)

    fun savePlace(latitude: Double, longitude: Double, onSaved: () -> Unit) {
        if (title.isBlank()) return
        
        viewModelScope.launch {
            placeRepository.addPlace(
                title = title,
                description = description,
                emoji = emoji,
                latitude = latitude,
                longitude = longitude,
                photoPath = photoPath
            )
            onSaved()
        }
    }
    
    fun updatePhoto(path: String?) {
        photoPath = path
    }
}
