package com.example.myplaces.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myplaces.data.repository.PlaceRepository
import com.example.myplaces.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    val biometricLockEnabled: StateFlow<Boolean> = settingsRepository.biometricLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val authorName: StateFlow<String> = settingsRepository.authorName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricLockEnabled(enabled)
        }
    }

    fun setAuthorName(name: String) {
        viewModelScope.launch {
            settingsRepository.setAuthorName(name)
        }
    }

    suspend fun exportData(): File {
        val author = settingsRepository.currentAuthorName()
        return placeRepository.exportToJson(author)
    }
}
