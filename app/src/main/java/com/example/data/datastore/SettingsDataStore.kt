package com.example.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode") // "system", "dark", "light", "amoled"
        val THEME_COLOR_KEY = stringPreferencesKey("theme_color") // "NeonPurple", "Cyberpunk", "Emerald", etc.
        val FONT_KEY = stringPreferencesKey("app_font") // "Default", "SansSerif", "Monospace", etc.
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language") // "bn" or "en"
        val AUTO_PLAY_KEY = stringPreferencesKey("auto_play") // "true" or "false"
        val HQ_AUDIO_KEY = stringPreferencesKey("hq_audio") // "true" or "false"
        val STUDIO_FX_KEY = stringPreferencesKey("studio_fx") // "true" or "false"
        val NOTIFICATIONS_KEY = stringPreferencesKey("notifications") // "true" or "false"
        val LOGGED_IN_EMAIL_KEY = stringPreferencesKey("logged_in_email")
        val LOGGED_IN_ROLE_KEY = stringPreferencesKey("logged_in_role")
    }

    val loggedInEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[LOGGED_IN_EMAIL_KEY] }

    val loggedInRole: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[LOGGED_IN_ROLE_KEY] }

    suspend fun saveUserSession(email: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGED_IN_EMAIL_KEY] = email
            preferences[LOGGED_IN_ROLE_KEY] = role
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(LOGGED_IN_EMAIL_KEY)
            preferences.remove(LOGGED_IN_ROLE_KEY)
        }
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME_MODE_KEY] ?: "light" }

    val themeColor: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME_COLOR_KEY] ?: "NeonPurple" }

    val appFont: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[FONT_KEY] ?: "Default" }

    val appLanguage: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[APP_LANGUAGE_KEY] ?: "bn" }

    val autoPlay: Flow<Boolean> = context.dataStore.data
        .map { preferences -> (preferences[AUTO_PLAY_KEY] ?: "true") == "true" }

    val hqAudio: Flow<Boolean> = context.dataStore.data
        .map { preferences -> (preferences[HQ_AUDIO_KEY] ?: "true") == "true" }

    val studioFx: Flow<Boolean> = context.dataStore.data
        .map { preferences -> (preferences[STUDIO_FX_KEY] ?: "true") == "true" }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> (preferences[NOTIFICATIONS_KEY] ?: "true") == "true" }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setThemeColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color
        }
    }

    suspend fun setAppFont(font: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_KEY] = font
        }
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_KEY] = lang
        }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_PLAY_KEY] = enabled.toString()
        }
    }

    suspend fun setHqAudio(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HQ_AUDIO_KEY] = enabled.toString()
        }
    }

    suspend fun setStudioFx(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STUDIO_FX_KEY] = enabled.toString()
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled.toString()
        }
    }
}
