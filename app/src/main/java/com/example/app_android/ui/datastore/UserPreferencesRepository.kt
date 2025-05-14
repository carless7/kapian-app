package com.example.app_android.ui.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val SELECTED_CARD_KEY = stringPreferencesKey("selected_card")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }


    val selectedCard: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_CARD_KEY] ?: "No card selected"
        }


    suspend fun saveDarkModePreference(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isEnabled
        }
    }

    suspend fun saveSelectedCard(card: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CARD_KEY] = card
        }
    }


    suspend fun clearSelectedCard() {
        context.dataStore.edit { preferences ->
            preferences.remove(SELECTED_CARD_KEY)
        }
    }
}