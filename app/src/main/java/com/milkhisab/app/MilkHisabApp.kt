package com.milkhisab.app

import android.app.Application
import android.content.Context
import com.milkhisab.app.data.local.AppDatabase
import com.milkhisab.app.data.repository.MilkRepository
import com.milkhisab.app.data.settings.SettingsRepository
import com.milkhisab.app.ui.strings.Language
import com.milkhisab.app.ui.strings.Strings

/**
 * Tiny manual dependency container (no Hilt - keeps the project simple).
 * The database lives as long as the process, which is what gives us
 * data persistence across app restarts.
 */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val repository: MilkRepository = MilkRepository(database.milkRecordDao(), database)

    /** Language + theme preferences, kept out of Room on purpose. */
    val settingsRepository: SettingsRepository = SettingsRepository.create(context)
}

class MilkHisabApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Start from the default language so any message produced before the
        // saved preference is read is still in the right language.
        Strings.activate(Language.DEFAULT)
    }

    companion object {
        fun from(context: Context): MilkHisabApp =
            context.applicationContext as MilkHisabApp
    }
}
