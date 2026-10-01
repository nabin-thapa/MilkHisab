package com.milkhisab.app

import com.milkhisab.app.data.settings.AppSettings
import com.milkhisab.app.data.settings.SettingsStore
import com.milkhisab.app.data.settings.ThemeMode
import com.milkhisab.app.ui.strings.Language
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * In-memory stand-in for DataStore: the same contract, no Android.
 * Values survive "restarts" because it is held across ViewModel instances,
 * which is exactly what a fresh install versus a returning user needs.
 */
class FakeSettingsStore(
    initial: AppSettings = AppSettings.DEFAULT
) : SettingsStore {

    private val state = MutableStateFlow(initial)

    /** How many times each value was written, to prove writes happen. */
    var languageWrites = 0
        private set
    var themeWrites = 0
        private set

    override val settings: Flow<AppSettings> = state

    /** The stored value, as a real repository read would return it. */
    val current: AppSettings get() = state.value

    override suspend fun setLanguage(language: Language) {
        languageWrites++
        state.value = state.value.copy(language = language)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeWrites++
        state.value = state.value.copy(themeMode = mode)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        Strings.activate(Language.NEPALI)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Strings.activate(Language.NEPALI)
    }

    // ------------------------------------------------------------- defaults

    @Test
    fun `default language is Nepali`() {
        assertEquals(Language.NEPALI, AppSettings.DEFAULT.language)
        assertEquals(Language.DEFAULT, Language.NEPALI)
    }

    @Test
    fun `default theme follows the system`() {
        assertEquals(ThemeMode.SYSTEM, AppSettings.DEFAULT.themeMode)
        assertEquals(ThemeMode.SYSTEM, ThemeMode.DEFAULT)
    }

    @Test
    fun `unknown stored language falls back to Nepali`() {
        assertEquals(Language.NEPALI, Language.fromTag("fr"))
        assertEquals(Language.NEPALI, Language.fromTag(null))
        assertEquals(Language.NEPALI, Language.fromTag(""))
    }

    @Test
    fun `unknown stored theme falls back to system`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("sepia"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey(null))
    }

    @Test
    fun `known tags and keys are parsed back`() {
        assertEquals(Language.ENGLISH, Language.fromTag("en"))
        assertEquals(Language.NEPALI, Language.fromTag("NE"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromKey("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromKey("LIGHT"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("system"))
    }

    // --------------------------------------------------------- persistence

    @Test
    fun `language preference persists`() = runTest(dispatcher) {
        val store = FakeSettingsStore()
        val vm = SettingsViewModel(store)
        advanceUntilIdle()

        vm.onLanguageSelected(Language.ENGLISH)
        advanceUntilIdle()

        assertEquals(1, store.languageWrites)
        assertEquals(Language.ENGLISH, store.current.language)
        assertEquals(Language.ENGLISH, vm.settings.value.language)
    }

    @Test
    fun `theme preference persists`() = runTest(dispatcher) {
        val store = FakeSettingsStore()
        val vm = SettingsViewModel(store)
        advanceUntilIdle()

        vm.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(1, store.themeWrites)
        assertEquals(ThemeMode.DARK, vm.settings.value.themeMode)
    }

    @Test
    fun `saved preferences are restored on the next start`() = runTest(dispatcher) {
        val store = FakeSettingsStore(
            AppSettings(language = Language.ENGLISH, themeMode = ThemeMode.DARK)
        )
        val vm = SettingsViewModel(store)
        advanceUntilIdle()

        assertEquals(Language.ENGLISH, vm.settings.value.language)
        assertEquals(ThemeMode.DARK, vm.settings.value.themeMode)
        assertTrue("preferences must be marked as loaded", vm.isLoaded.value)
    }

    @Test
    fun `switching language twice keeps the last choice`() = runTest(dispatcher) {
        val store = FakeSettingsStore()
        val vm = SettingsViewModel(store)
        advanceUntilIdle()

        vm.onLanguageSelected(Language.ENGLISH)
        advanceUntilIdle()
        vm.onLanguageSelected(Language.NEPALI)
        advanceUntilIdle()

        assertEquals(2, store.languageWrites)
        assertEquals(Language.NEPALI, vm.settings.value.language)
    }

    @Test
    fun `switching theme twice keeps the last choice`() = runTest(dispatcher) {
        val store = FakeSettingsStore()
        val vm = SettingsViewModel(store)
        advanceUntilIdle()

        vm.onThemeSelected(ThemeMode.LIGHT)
        advanceUntilIdle()
        vm.onThemeSelected(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(2, store.themeWrites)
        assertEquals(ThemeMode.DARK, vm.settings.value.themeMode)
    }

    // ------------------------------------------------- language side effect

    @Test
    fun `choosing a language switches the active catalogue instantly`() =
        runTest(dispatcher) {
            val vm = SettingsViewModel(FakeSettingsStore())
            advanceUntilIdle()
            assertEquals("आज", Strings.current.today)

            vm.onLanguageSelected(Language.ENGLISH)
            advanceUntilIdle()

            assertEquals("Today", Strings.current.today)
            assertEquals(Strings.ENGLISH, Strings.current)
        }

    @Test
    fun `catalogue lookup by language`() {
        assertEquals(Strings.NEPALI, Strings.of(Language.NEPALI))
        assertEquals(Strings.ENGLISH, Strings.of(Language.ENGLISH))
        assertNotEquals(Strings.NEPALI.today, Strings.ENGLISH.today)
    }
}
