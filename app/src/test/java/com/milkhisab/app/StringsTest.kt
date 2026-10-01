package com.milkhisab.app

import com.milkhisab.app.ui.strings.Language
import com.milkhisab.app.ui.strings.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Guards the bilingual contract.
 *
 * These tests fail loudly if someone adds a language and forgets to fill in
 * a string, or if a label is left blank - the two ways a bilingual app
 * usually rots.
 */
class StringsTest {

    private val languages = Strings.all

    // ---------------------------------------------------------- completeness

    @Test
    fun `both languages are available`() {
        assertEquals(2, languages.size)
        assertTrue(languages.contains(Strings.NEPALI))
        assertTrue(languages.contains(Strings.ENGLISH))
    }

    @Test
    fun `no string is blank in any language`() {
        val blank = languages.flatMap { catalogue ->
            catalogue.textFields()
                .filter { (_, value) -> value.isBlank() }
                .map { (field, _) -> "${catalogue.tagOf()}.$field" }
        }
        assertTrue("blank strings: $blank", blank.isEmpty())
    }

    @Test
    fun `no string is an untranslated copy of the other language`() {
        // A handful of words are intentionally identical (brand name,
        // currency, "L"). Everything else must actually differ.
        val shared = mutableListOf<String>()
        val ne = Strings.NEPALI.textFields()
        val en = Strings.ENGLISH.textFields()
        for ((field, neValue) in ne) {
            val enValue = en.getValue(field)
            if (neValue == enValue) shared.add(field)
        }
        // A few values are intentionally identical: the unit "L", the
        // currency prefix, and the two language names, which always appear
        // in their own script.
        val allowed = setOf(
            "litreshort",
            "litres",
            "currencyprefix",
            "languagenepali",
            "languageenglish"
        )
        val unexpected = shared - allowed
        assertTrue("untranslated: $unexpected", unexpected.isEmpty())
    }

    @Test
    fun `every month and weekday is translated`() {
        for (catalogue in languages) {
            assertEquals(12, catalogue.months.size)
            assertEquals(7, catalogue.weekdays.size)
            assertTrue(catalogue.months.none { it.isBlank() })
            assertTrue(catalogue.weekdays.none { it.isBlank() })
        }
        assertNotEquals(Strings.NEPALI.months, Strings.ENGLISH.months)
        assertNotEquals(Strings.NEPALI.weekdays, Strings.ENGLISH.weekdays)
    }

    // ------------------------------------------------------ required content

    @Test
    fun `nepali uses the expected everyday wording`() {
        val ne = Strings.NEPALI
        assertEquals("गृह", ne.navHome)
        assertEquals("रेकर्ड", ne.navRecords)
        assertEquals("हिसाब", ne.navSummary)
        assertEquals("आजको दूध", ne.todayMilk)
        assertEquals("दूधको मात्रा", ne.quantityLabel)
        assertEquals("प्रति लिटर दर", ne.rateFieldLabel)
        assertEquals("जम्मा रकम", ne.amountLabel)
        assertEquals("सम्पादन गर्नुहोस्", ne.duplicateEdit)
        assertEquals("हटाउनुहोस्", ne.confirmDelete)
        assertEquals("रेकर्ड सेभ गर्नुहोस्", ne.saveRecord)
    }

    @Test
    fun `english uses the expected wording`() {
        val en = Strings.ENGLISH
        assertEquals("Home", en.navHome)
        assertEquals("Records", en.navRecords)
        assertEquals("Summary", en.navSummary)
        assertEquals("Today's milk", en.todayMilk)
        assertEquals("Quantity", en.quantityLabel)
        assertEquals("Rate per litre", en.rateFieldLabel)
        assertEquals("Total amount", en.amountLabel)
        assertEquals("Edit", en.duplicateEdit)
        assertEquals("Delete", en.confirmDelete)
        assertEquals("Save record", en.saveRecord)
    }

    @Test
    fun `settings labels exist in both languages`() {
        for (catalogue in languages) {
            assertFalse(catalogue.settingsTitle.isBlank())
            assertFalse(catalogue.language.isBlank())
            assertFalse(catalogue.theme.isBlank())
            assertFalse(catalogue.themeLight.isBlank())
            assertFalse(catalogue.themeDark.isBlank())
            assertFalse(catalogue.themeSystem.isBlank())
            assertFalse(catalogue.backupCreate.isBlank())
            assertFalse(catalogue.backupRestore.isBlank())
            assertFalse(catalogue.exportMonthReport.isBlank())
            // The two language names are always shown in their own script.
            assertEquals("नेपाली", catalogue.languageNepali)
            assertEquals("English", catalogue.languageEnglish)
        }
    }

    @Test
    fun `dialogs and feedback are fully translated`() {
        for (catalogue in languages) {
            assertFalse(catalogue.duplicateTitle.isBlank())
            assertFalse(catalogue.duplicateMessage.isBlank())
            assertFalse(catalogue.deleteTitle.isBlank())
            assertFalse(catalogue.deleteMessage.isBlank())
            assertFalse(catalogue.backupRestoreTitle.isBlank())
            assertFalse(catalogue.backupRestoreMessage.isBlank())
            assertFalse(catalogue.msgSaved.isBlank())
            assertFalse(catalogue.msgUpdated.isBlank())
            assertFalse(catalogue.msgDeleted.isBlank())
            assertFalse(catalogue.errQuantityRequired.isBlank())
            assertFalse(catalogue.errRateRequired.isBlank())
            assertFalse(catalogue.errSaveFailed.isBlank())
            assertFalse(catalogue.recordsEmpty.isBlank())
        }
        // The Nepali duplicate message is fixed wording the user asked for.
        assertEquals(
            "यो मितिको रेकर्ड पहिले नै छ। के तपाईं पुरानो रेकर्ड सम्पादन गर्न चाहनुहोस्?",
            Strings.NEPALI.duplicateMessage
        )
    }

    @Test
    fun `accessibility labels are translated`() {
        for (catalogue in languages) {
            assertFalse(catalogue.cdEdit.isBlank())
            assertFalse(catalogue.cdDelete.isBlank())
            assertFalse(catalogue.cdBack.isBlank())
            assertFalse(catalogue.cdAdd.isBlank())
            assertFalse(catalogue.cdSettings.isBlank())
            assertFalse(catalogue.cdExportMonth.isBlank())
        }
    }

    @Test
    fun `date formatting follows the chosen language`() {
        val date = LocalDate.of(2026, 10, 1)
        val month = YearMonth.of(2026, 10)

        assertEquals("1 अक्टोबर 2026", com.milkhisab.app.utils.Formatters.dateFull(date, Strings.NEPALI))
        assertEquals("1 October 2026", com.milkhisab.app.utils.Formatters.dateFull(date, Strings.ENGLISH))
        assertEquals("अक्टोबर 2026", com.milkhisab.app.utils.Formatters.monthTitle(month, Strings.NEPALI))
        assertEquals("October 2026", com.milkhisab.app.utils.Formatters.monthTitle(month, Strings.ENGLISH))
    }

    @Test
    fun `relative day labels follow the chosen language`() {
        val today = LocalDate.of(2026, 10, 1)
        val f = com.milkhisab.app.utils.Formatters
        assertEquals("आज", f.dateShort(today, today, Strings.NEPALI))
        assertEquals("Today", f.dateShort(today, today, Strings.ENGLISH))
        assertEquals("हिजो", f.dateShort(today.minusDays(1), today, Strings.NEPALI))
        assertEquals("Yesterday", f.dateShort(today.minusDays(1), today, Strings.ENGLISH))
    }

    @Test
    fun `day count is pluralised correctly`() {
        val f = com.milkhisab.app.utils.Formatters
        assertEquals("1 day", f.dayCount(1, Strings.ENGLISH))
        assertEquals("2 days", f.dayCount(2, Strings.ENGLISH))
        assertEquals("0 days", f.dayCount(0, Strings.ENGLISH))
        // Nepali does not inflect the unit.
        assertEquals("1 दिन", f.dayCount(1, Strings.NEPALI))
        assertEquals("7 दिन", f.dayCount(7, Strings.NEPALI))
    }

    @Test
    fun `greeting follows the chosen language`() {        val f = com.milkhisab.app.utils.Formatters
        assertEquals("शुभ प्रभात", f.greeting(java.time.LocalTime.of(8, 0), Strings.NEPALI))
        assertEquals("शुभ सन्ध्या", f.greeting(java.time.LocalTime.of(20, 0), Strings.NEPALI))
        assertEquals("Good morning", f.greeting(java.time.LocalTime.of(8, 0), Strings.ENGLISH))
        assertEquals("Good evening", f.greeting(java.time.LocalTime.of(20, 0), Strings.ENGLISH))
    }

    // ------------------------------------------------------------- helpers

    private fun Strings.tagOf(): String =
        if (this === Strings.NEPALI) "ne" else "en"

    /** Every user-visible String field, as name/value pairs. */
    private fun Strings.textFields(): Map<String, String> {
        val fields = LinkedHashMap<String, String>()
        // Plain Java reflection over the data class getters, so a newly
        // added string is covered by the completeness tests automatically.
        this::class.java.methods
            .filter { it.parameterCount == 0 && it.name.startsWith("get") }
            .forEach { method ->
                val value = runCatching { method.invoke(this) }.getOrNull()
                if (value is String) fields[method.name.removePrefix("get").lowercase()] = value
            }
        return fields
    }
}
