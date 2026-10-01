package com.milkhisab.app.data.settings

import com.milkhisab.app.ui.strings.Language

/**
 * How the app decides between the light and the dark colour scheme.
 *
 * The stored key is short and stable so a future rename cannot orphan
 * somebody's saved choice.
 */
enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        val DEFAULT = SYSTEM

        /** Unknown values fall back to [DEFAULT] rather than crashing. */
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DEFAULT
    }
}

/**
 * UI preferences only. Milk records live in Room and are never mixed in
 * here - this store holds two small scalars.
 */
data class AppSettings(
    val language: Language = Language.DEFAULT,
    val themeMode: ThemeMode = ThemeMode.DEFAULT
) {
    companion object {
        /** What a fresh install (or a corrupted store) falls back to. */
        val DEFAULT = AppSettings()
    }
}
