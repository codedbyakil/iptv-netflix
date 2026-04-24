package com.tamilflix.iptv.settings
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
object AppSettings {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[DARK_MODE] = enabled }
    }
    val darkModeFlow: Flow<Boolean> = preferencesDataStore(name = "settings").data.map { preferences -> preferences[DARK_MODE] ?: true }
}
