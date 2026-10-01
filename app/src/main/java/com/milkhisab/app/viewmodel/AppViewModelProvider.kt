package com.milkhisab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.milkhisab.app.MilkHisabApp
import com.milkhisab.app.utils.DateProvider

/**
 * One place that knows how to build every ViewModel.
 *
 * The plain [Factory] reads the Application from [CreationExtras]
 * (APPLICATION_KEY), so screens never touch the Application object.
 * The add/edit screen additionally needs its recordId argument, hence
 * [factoryForAddEdit].
 */
object AppViewModelProvider {

    /** The Application instance the framework hands us in [CreationExtras]. */
    private fun CreationExtras.app(): MilkHisabApp =
        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MilkHisabApp

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(app().container.repository, DateProvider())
        }
        initializer {
            RecordsViewModel(app().container.repository, DateProvider())
        }
        initializer {
            SummaryViewModel(app().container.repository, DateProvider())
        }
        initializer {
            BackupViewModel(app().container.repository)
        }
        initializer {
            SettingsViewModel(app().container.settingsRepository)
        }
    }

    /**
     * The add/edit screen needs its arguments (recordId) at construction
     * time, so it gets a small purpose-built factory. It still resolves the
     * Application through CreationExtras - no global state.
     */
    fun factoryForAddEdit(recordId: Long?): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as MilkHisabApp
                @Suppress("UNCHECKED_CAST")
                return AddEditViewModel(
                    repository = app.container.repository,
                    dateProvider = DateProvider(),
                    initialRecordId = recordId?.takeIf { it > 0L }
                ) as T
            }
        }
}
