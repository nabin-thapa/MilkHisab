package com.milkhisab.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.milkhisab.app.ui.strings.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** One preferences file for the whole app, created lazily. */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "milkhisab_settings"
)

/**
 * The contract the settings screen depends on.
 *
 * Keeping this as an interface means the DataStore implementation can be
 * swapped for an in-memory fake in unit tests, so language and theme
 * behaviour is verified without an emulator.
 */
interface SettingsStore {
    /** Current preferences; emits the defaults until something is stored. */
    val settings: Flow<AppSettings>

    suspend fun setLanguage(language: Language)

    suspend fun setThemeMode(mode: ThemeMode)
}

/**
 * Persists the language and the theme choice.
 *
 * Deliberately tiny and independent of Room: a corrupted or unreadable
 * settings file can only ever cost the user their theme choice, never a
 * single milk record. Reads are a cold flow, writes are suspending.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) : SettingsStore {

    /**
     * Current preferences, starting from the defaults when the file has not
     * been written yet (fresh install) or cannot be read.
     */
    override val settings: Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            // A broken file must not take the whole UI down.
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { prefs ->
            AppSettings(
                language = Language.fromTag(prefs[KEY_LANGUAGE]),
                themeMode = ThemeMode.fromKey(prefs[KEY_THEME])
            )
        }

    override suspend fun setLanguage(language: Language) {
        dataStore.edit { it[KEY_LANGUAGE] = language.tag }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.key }
    }

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")

        fun create(context: Context): SettingsRepository =
            SettingsRepository(context.applicationContext.settingsDataStore)
    }
}
