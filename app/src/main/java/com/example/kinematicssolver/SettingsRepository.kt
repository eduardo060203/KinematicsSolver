// Contenido para SettingsRepository.kt

package com.example.kinematicssolver

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 1. Definimos la instancia de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // 2. Definimos las "llaves" para nuestros valores
    companion object {
        val L1_KEY = stringPreferencesKey("l1_input_cm")
        val L2_KEY = stringPreferencesKey("l2_input_cm")
    }

    // 3. Creamos una función para LEER los valores
    //    Devuelve un Flow con los valores guardados, o los valores por defecto si no hay nada.
    val getSettings: Flow<Pair<String, String>> = context.dataStore.data
        .map { preferences ->
            val l1 = preferences[L1_KEY] ?: "18.0" // Valor por defecto
            val l2 = preferences[L2_KEY] ?: "20.0" // Valor por defecto
            Pair(l1, l2)
        }

    // 4. Creamos una función para GUARDAR los valores
    suspend fun saveSettings(l1: String, l2: String) {
        context.dataStore.edit { preferences ->
            preferences[L1_KEY] = l1
            preferences[L2_KEY] = l2
        }
    }
}

