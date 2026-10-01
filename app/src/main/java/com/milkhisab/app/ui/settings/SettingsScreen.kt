package com.milkhisab.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milkhisab.app.BuildConfig
import com.milkhisab.app.data.settings.ThemeMode
import com.milkhisab.app.ui.backup.DataToolsSection
import com.milkhisab.app.ui.components.ChoiceRow
import com.milkhisab.app.ui.components.Label
import com.milkhisab.app.ui.components.MilkCard
import com.milkhisab.app.ui.components.SettingsSectionLabel
import com.milkhisab.app.ui.strings.Language
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.theme.Space
import com.milkhisab.app.viewmodel.AppViewModelProvider
import com.milkhisab.app.viewmodel.SettingsViewModel

/**
 * सेटिङ / Settings.
 *
 * Four quiet sections. Language and theme take effect immediately - the
 * whole app recomposes in the new language or the new colour scheme, with
 * no restart and no lost data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s = LocalStrings.current
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(s.settingsTitle, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.cdBack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = Space.xxxl)
        ) {

            // ------------------------------------------------------- general
            SettingsSectionLabel(text = s.sectionGeneral)
            Column(
                modifier = Modifier.padding(horizontal = Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                ChoiceRow(
                    title = s.languageNepali,
                    selected = settings.language == Language.NEPALI,
                    onClick = { viewModel.onLanguageSelected(Language.NEPALI) }
                )
                ChoiceRow(
                    title = s.languageEnglish,
                    selected = settings.language == Language.ENGLISH,
                    onClick = { viewModel.onLanguageSelected(Language.ENGLISH) }
                )
            }

            // ---------------------------------------------------- appearance
            SettingsSectionLabel(text = s.sectionAppearance)
            Column(
                modifier = Modifier.padding(horizontal = Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                ChoiceRow(
                    title = s.themeSystem,
                    selected = settings.themeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.onThemeSelected(ThemeMode.SYSTEM) }
                )
                ChoiceRow(
                    title = s.themeLight,
                    selected = settings.themeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.onThemeSelected(ThemeMode.LIGHT) }
                )
                ChoiceRow(
                    title = s.themeDark,
                    selected = settings.themeMode == ThemeMode.DARK,
                    onClick = { viewModel.onThemeSelected(ThemeMode.DARK) }
                )
            }

            // ---------------------------------------------------------- data
            SettingsSectionLabel(text = s.sectionData)
            DataToolsSection(onMessage = onMessage)

            // --------------------------------------------------------- about
            SettingsSectionLabel(text = s.sectionAbout)
            MilkCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space.xl)
                ) {
                    Text(
                        text = s.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = s.appSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Space.md))
                    Text(
                        text = String.format(
                            s.appVersionFormat,
                            BuildConfig.VERSION_NAME
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(Space.sm))
                    Label(text = s.offlineNote)
                }
            }
        }
    }
}
