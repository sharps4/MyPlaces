package com.example.myplaces.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        val AUTHOR_NAME = stringPreferencesKey("author_name")
    }

    private val preferences: Flow<Preferences> = store.data.catch { throwable ->
        // Un fichier de préférences corrompu ne doit pas empêcher l'app de démarrer.
        if (throwable is IOException) emit(emptyPreferences()) else throw throwable
    }

    val biometricLockEnabled: Flow<Boolean> =
        preferences.map { it[Keys.BIOMETRIC_LOCK] ?: false }

    val authorName: Flow<String> =
        preferences.map { it[Keys.AUTHOR_NAME].orEmpty() }

    suspend fun isBiometricLockEnabled(): Boolean = biometricLockEnabled.first()

    suspend fun currentAuthorName(): String = authorName.first()

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        store.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }

    suspend fun setAuthorName(name: String) {
        store.edit { it[Keys.AUTHOR_NAME] = name.trim() }
    }
}
