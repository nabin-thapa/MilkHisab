package com.milkhisab.app.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The active language, provided once at the top of the app.
 *
 * Every screen reads its text from here, which is why switching language
 * updates the whole UI instantly (a plain recomposition - no restart, and
 * no rotation of the Activity).
 */
val LocalStrings = staticCompositionLocalOf { Strings.NEPALI }

/** Shorthand used inside composables: `strings.appName`. */
val strings: Strings
    @Composable
    @ReadOnlyComposable
    get() = LocalStrings.current
