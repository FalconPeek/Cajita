package com.example.cajita.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DISPOSITIVO_ID = stringPreferencesKey("dispositivo_id")
        val ULTIMO_CAJERO = stringPreferencesKey("ultimo_cajero")
    }

    val dispositivoIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DISPOSITIVO_ID] ?: ""
    }

    val ultimoCajeroFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ULTIMO_CAJERO] ?: ""
    }

    suspend fun getOrCreateDispositivoId(): String {
        val currentId = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DISPOSITIVO_ID]
        }.first()

        if (currentId != null && currentId.isNotEmpty()) {
            return currentId
        }

        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPOSITIVO_ID] = newId
        }
        return newId
    }

    suspend fun saveUltimoCajero(nombre: String) {
        val nombreLimpio = nombre.trim()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ULTIMO_CAJERO] = nombreLimpio
        }
    }
}
