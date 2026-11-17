package com.universalpiano.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "universal_piano_settings")

/** Persistência local (DataStore). Nenhum dado sai do aparelho. */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            themeMode = parseEnumOrDefault(prefs[KEY_THEME], ThemeMode.SYSTEM),
            accent = parseEnumOrDefault(prefs[KEY_ACCENT], AccentPalette.VIOLET),
            glissando = prefs[KEY_GLISSANDO] ?: true,
            keySensitivity = (prefs[KEY_SENSITIVITY] ?: 0.5f).coerceIn(0f, 1f),
        )
    }

    suspend fun setThemeMode(value: ThemeMode) = store.edit { it[KEY_THEME] = value.name }
    suspend fun setAccent(value: AccentPalette) = store.edit { it[KEY_ACCENT] = value.name }
    suspend fun setGlissando(value: Boolean) = store.edit { it[KEY_GLISSANDO] = value }
    suspend fun setKeySensitivity(value: Float) = store.edit { it[KEY_SENSITIVITY] = value.coerceIn(0f, 1f) }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_ACCENT = stringPreferencesKey("accent")
        val KEY_GLISSANDO = booleanPreferencesKey("glissando")
        val KEY_SENSITIVITY = floatPreferencesKey("key_sensitivity")
    }
}
