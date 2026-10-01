package com.milkhisab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.milkhisab.app.data.settings.ThemeMode
import com.milkhisab.app.navigation.MilkBottomBar
import com.milkhisab.app.navigation.MilkNavHost
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.ui.theme.MilkHisabTheme
import com.milkhisab.app.viewmodel.AppViewModelProvider
import com.milkhisab.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            val settings by settingsViewModel.settings.collectAsState()
            val settingsLoaded by settingsViewModel.isLoaded.collectAsState()

            // Non-Compose code (ViewModel snackbar messages) reads the active
            // catalogue, so keep it in step with the saved preference.
            LaunchedEffect(settings.language) {
                Strings.activate(settings.language)
            }

            val darkTheme = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MilkHisabTheme(darkTheme = darkTheme) {
                // One place where the app's language is provided: every
                // screen below reads from LocalStrings.
                CompositionLocalProvider(
                    LocalStrings provides Strings.of(settings.language)
                ) {
                    if (!settingsLoaded) {
                        // One blank frame while the saved preferences arrive,
                        // instead of a flash of the wrong language or theme.
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {}
                    } else {
                        val navController = rememberNavController()
                        val snackbarHostState = remember { SnackbarHostState() }
                        val scope = rememberCoroutineScope()

                        // Single funnel for every "रेकर्ड सेभ भयो।" style message.
                        val showMessage: (String) -> Unit = { text ->
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(text)
                            }
                        }

                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = { MilkBottomBar(navController) },
                            containerColor = MaterialTheme.colorScheme.background
                        ) { paddingValues ->
                            MilkNavHost(
                                navController = navController,
                                modifier = Modifier.padding(paddingValues),
                                onMessage = showMessage
                            )
                        }
                    }
                }
            }
        }
    }
}
