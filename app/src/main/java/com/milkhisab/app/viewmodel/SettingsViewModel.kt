package com.milkhisab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkhisab.app.data.settings.AppSettings
import com.milkhisab.app.data.settings.SettingsStore
import com.milkhisab.app.data.settings.ThemeMode
import com.milkhisab.app.ui.strings.Language
import com.milkhisab.app.ui.strings.Strings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Language + theme preferences.
 *
 * The state survives process death because it comes straight from
 * DataStore; the UI observes it and reacts, so nothing here needs to
 * remember screen state.
 */
class SettingsViewModel(
    private val repository: SettingsStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings.DEFAULT
        )

    /**
     * False until the stored preferences have been read once. The UI waits
     * for this so a user who chose English or dark mode never sees a frame
     * of the default language or theme.
     */
    val isLoaded: StateFlow<Boolean> = repository.settings
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun onLanguageSelected(language: Language) {
        // Non-UI code (ViewModel messages) reads the active strings, so
        // refresh it right away - snackbars stay in the chosen language.
        Strings.activate(language)
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }
}
