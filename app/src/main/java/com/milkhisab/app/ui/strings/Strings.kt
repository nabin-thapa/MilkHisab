package com.milkhisab.app.ui.strings

/**
 * The two languages the app speaks.
 *
 * [tag] is what gets written to DataStore, so the stored value stays short
 * and stable ("ne" / "en").
 */
enum class Language(val tag: String) {
    NEPALI("ne"),
    ENGLISH("en");

    companion object {
        /** The app is Nepali-first: this is what a fresh install shows. */
        val DEFAULT = NEPALI

        /** Unknown / missing values fall back to [DEFAULT] instead of crashing. */
        fun fromTag(tag: String?): Language =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: DEFAULT
    }
}

/**
 * Every user-facing string in the app, for one language.
 *
 * Design rules:
 *  - one flat data class, so adding a language can never leave a string out
 *    (the compiler catches it instead of the user seeing a blank label);
 *  - Composables read the active instance through `LocalStrings`;
 *  - plain Kotlin (no Compose) code passes an instance explicitly or uses
 *    [current];
 *  - no screen ever builds a sentence out of raw literals - formatting is
 *    done by `Formatters`, which is unit tested.
 */
data class Strings(
    // ----------------------------------------------------------- identity
    val appName: String,
    val appSubtitle: String,
    // --------------------------------------------------------- navigation
    val navHome: String,
    val navRecords: String,
    val navSummary: String,
    // ---------------------------------------------------------------- home
    val greetingMorning: String,
    val greetingAfternoon: String,
    val greetingEvening: String,
    val today: String,
    val todayDatePrefix: String,
    val todayMilk: String,
    val todayMilkMissing: String,
    val rateLabel: String,
    val todayTotal: String,
    val addTodayMilk: String,
    val editRecord: String,
    val thisMonth: String,
    val totalMilk: String,
    val totalAmount: String,
    val recordedDays: String,
    val daysUnit: String,
    val daysUnitSingular: String,
    val viewRecords: String,
    val monthlySummary: String,
    val averageDailyMilk: String,
    val moreOptions: String,
    // --------------------------------------------------------- add / edit
    val addTitle: String,
    val editTitle: String,
    val dateLabel: String,
    val changeDate: String,
    val quantityLabel: String,
    val quantityHint: String,
    val litres: String,
    val litreShort: String,
    val rateFieldLabel: String,
    val rateHint: String,
    val rupeesUnit: String,
    val amountLabel: String,
    val amountAutoNote: String,
    val saveRecord: String,
    val cancel: String,
    val quickQuantityTitle: String,
    val pickDate: String,
    val dateDone: String,
    val deleteRecord: String,
    // ------------------------------------------------------------- records
    val recordsTitle: String,
    val recordsEmpty: String,
    val firstRecord: String,
    val yesterday: String,
    // ------------------------------------------------------------- summary
    val summaryTitle: String,
    val thisMonthTitle: String,
    val previousMonth: String,
    val nextMonth: String,
    val dailyRecords: String,
    val monthEmpty: String,
    val grandTotal: String,
    val exportMonthReport: String,
    // ------------------------------------------------------------- dialogs
    val duplicateTitle: String,
    val duplicateMessage: String,
    val duplicateEdit: String,
    val deleteTitle: String,
    val deleteMessage: String,
    val confirmDelete: String,
    // ------------------------------------------------------------ feedback
    val msgSaved: String,
    val msgUpdated: String,
    val msgDeleted: String,
    val msgBackupCreated: String,
    val msgBackupRestored: String,
    val msgMonthExported: String,
    // -------------------------------------------------------------- errors
    val errQuantityRequired: String,
    val errQuantityMustBePositive: String,
    val errQuantityTooLarge: String,
    val errRateRequired: String,
    val errRateMustBePositive: String,
    val errRateTooLarge: String,
    val errInvalidNumber: String,
    val errSaveFailed: String,
    val errDateInvalid: String,
    val errBackupCreate: String,
    val errBackupRestore: String,
    val errExport: String,
    val errNothingToBackup: String,
    // ---------------------------------------------------------- data tools
    val backupTitle: String,
    val backupCreate: String,
    val backupRestore: String,
    val backupRestoreTitle: String,
    val backupRestoreMessage: String,
    val backupConfirm: String,
    val recordCountFormat: String,
    // ------------------------------------------------------------ settings
    val settingsTitle: String,
    val sectionGeneral: String,
    val sectionAppearance: String,
    val sectionData: String,
    val sectionAbout: String,
    val language: String,
    val languageNepali: String,
    val languageEnglish: String,
    val theme: String,
    val themeLight: String,
    val themeDark: String,
    val themeSystem: String,
    val appVersionFormat: String,
    val offlineNote: String,
    // ------------------------------------------------------- accessibility
    val cdEdit: String,
    val cdDelete: String,
    val cdBack: String,
    val cdAdd: String,
    val cdSettings: String,
    val cdMoreMenu: String,
    val cdExportMonth: String,
    // --------------------------------------------------------------- units
    val currencyPrefix: String,
    // ------------------------------------------------------- date names
    val months: List<String>,
    val weekdays: List<String>
) {
    companion object {

        // ============================================================ नेपाली
        val NEPALI = Strings(
            appName = "दूध हिसाब",
            appSubtitle = "दैनिक दूध रेकर्ड",
            navHome = "गृह",
            navRecords = "रेकर्ड",
            navSummary = "हिसाब",
            greetingMorning = "शुभ प्रभात",
            greetingAfternoon = "शुभ दिन",
            greetingEvening = "शुभ सन्ध्या",
            today = "आज",
            todayDatePrefix = "आज:",
            todayMilk = "आजको दूध",
            todayMilkMissing = "आजको दूधको रेकर्ड राखिएको छैन।",
            rateLabel = "दर",
            todayTotal = "आजको जम्मा",
            addTodayMilk = "आजको दूध थप्नुहोस्",
            editRecord = "सम्पादन गर्नुहोस्",
            thisMonth = "यो महिना",
            totalMilk = "कुल दूध",
            totalAmount = "कुल रकम",
            recordedDays = "रेकर्ड भएका दिन",
            daysUnit = "दिन",
            daysUnitSingular = "दिन",
            viewRecords = "रेकर्ड हेर्नुहोस्",
            monthlySummary = "महिनाको हिसाब",
            averageDailyMilk = "औसत दैनिक दूध",
            moreOptions = "थप विकल्प",
            addTitle = "दूध थप्नुहोस्",
            editTitle = "रेकर्ड सम्पादन गर्नुहोस्",
            dateLabel = "मिति",
            changeDate = "मिति बदल्नुहोस्",
            quantityLabel = "दूधको मात्रा",
            quantityHint = "जस्तै 3, 2.5, 1.5",
            litres = "लिटर",
            litreShort = "L",
            rateFieldLabel = "प्रति लिटर दर",
            rateHint = "जस्तै 70",
            rupeesUnit = "रुपैयाँ",
            amountLabel = "जम्मा रकम",
            amountAutoNote = "रकम आफैँ गणना हुन्छ।",
            saveRecord = "रेकर्ड सेभ गर्नुहोस्",
            cancel = "रद्द गर्नुहोस्",
            quickQuantityTitle = "छिटो मात्रा छान्नुहोस्",
            pickDate = "मिति छान्नुहोस्",
            dateDone = "ठीक छ",
            deleteRecord = "हटाउनुहोस्",
            recordsTitle = "पुराना रेकर्ड",
            recordsEmpty = "अहिलेसम्म कुनै दूध रेकर्ड छैन।",
            firstRecord = "पहिलो रेकर्ड थप्नुहोस्",
            yesterday = "हिजो",
            summaryTitle = "हिसाब",
            thisMonthTitle = "यो महिना",
            previousMonth = "अघिल्लो महिना",
            nextMonth = "अर्को महिना",
            dailyRecords = "दैनिक रेकर्ड",
            monthEmpty = "यस महिनामा कुनै रेकर्ड छैन।",
            grandTotal = "यस महिनाको जम्मा",
            exportMonthReport = "महिनाको रिपोर्ट निर्यात (CSV)",
            duplicateTitle = "रेकर्ड पहिले नै छ",
            duplicateMessage = "यो मितिको रेकर्ड पहिले नै छ। के तपाईं पुरानो रेकर्ड सम्पादन गर्न चाहनुहोस्?",
            duplicateEdit = "सम्पादन गर्नुहोस्",
            deleteTitle = "रेकर्ड हटाउने हो?",
            deleteMessage = "यो रेकर्ड हटाउने हो?",
            confirmDelete = "हटाउनुहोस्",
            msgSaved = "रेकर्ड सेभ भयो।",
            msgUpdated = "रेकर्ड अपडेट भयो।",
            msgDeleted = "रेकर्ड हटाइयो।",
            msgBackupCreated = "ब्याकअप फाइल बन्यो।",
            msgBackupRestored = "ब्याकअपबाट रेकर्ड फिर्ता आयो।",
            msgMonthExported = "महिनाको रिपोर्ट फाइल बन्यो।",
            errQuantityRequired = "कृपया दूधको मात्रा राख्नुहोस्।",
            errQuantityMustBePositive = "मात्रा ० भन्दा ठूलो हुनुपर्छ।",
            errQuantityTooLarge = "मात्रा धेरै ठूलो छ।",
            errRateRequired = "कृपया दूधको दर राख्नुहोस्।",
            errRateMustBePositive = "दर ० भन्दा ठूलो हुनुपर्छ।",
            errRateTooLarge = "दर धेरै ठूलो छ।",
            errInvalidNumber = "कृपया सही नम्बर लेख्नुहोस्।",
            errSaveFailed = "रेकर्ड सेभ गर्न सकिएन। कृपया फेरि प्रयास गर्नुहोस्।",
            errDateInvalid = "कृपया सही मिति राख्नुहोस्।",
            errBackupCreate = "ब्याकअप बनाउन सकिएन।",
            errBackupRestore = "ब्याकअप पढ्न सकिएन। फाइल सही छैन।",
            errExport = "रिपोर्ट बनाउन सकिएन।",
            errNothingToBackup = "ब्याकअप गर्न कुनै रेकर्ड छैन।",
            backupTitle = "ब्याकअप",
            backupCreate = "ब्याकअप फाइल बनाउनुहोस्",
            backupRestore = "ब्याकअपबाट रेकर्ड फिर्ता ल्याउनुहोस्",
            backupRestoreTitle = "ब्याकअप फिर्ता ल्याउने हो?",
            backupRestoreMessage = "फाइलबाट सबै रेकर्ड फिर्ता लिइनेछ र हालका रेकर्ड बदलिनेछन्। निश्चित हो?",
            backupConfirm = "फिर्ता ल्याउनुहोस्",
            recordCountFormat = "(%d वटा रेकर्ड)",
            settingsTitle = "सेटिङ",
            sectionGeneral = "सामान्य",
            sectionAppearance = "देखावट",
            sectionData = "डाटा",
            sectionAbout = "यसबारे",
            language = "भाषा",
            languageNepali = "नेपाली",
            languageEnglish = "English",
            theme = "थिम",
            themeLight = "उज्यालो",
            themeDark = "अँध्यारो",
            themeSystem = "प्रणालीअनुसार",
            appVersionFormat = "संस्करण %s",
            offlineNote = "पूरै अफलाइन चल्छ। खाता र इन्टरनेट चाहिँदैन।",
            cdEdit = "सम्पादन गर्नुहोस्",
            cdDelete = "हटाउनुहोस्",
            cdBack = "पछाडि जानुहोस्",
            cdAdd = "नयाँ रेकर्ड थप्नुहोस्",
            cdSettings = "सेटिङ खोल्नुहोस्",
            cdMoreMenu = "थप विकल्प",
            cdExportMonth = "महिनाको रिपोर्ट निर्यात गर्नुहोस्",
            currencyPrefix = "Rs.",
            months = listOf(
                "जनवरी", "फेब्रुअरी", "मार्च", "अप्रिल", "मे", "जुन",
                "जुलाई", "अगस्ट", "सेप्टेम्बर", "अक्टोबर", "नोभेम्बर", "डिसेम्बर"
            ),
            weekdays = listOf(
                "आइतबार", "सोमबार", "मंगलबार", "बुधबार", "बिहीबार", "शुक्रबार", "शनिबार"
            )
        )

        // ========================================================== English
        val ENGLISH = Strings(
            appName = "Milk Hisab",
            appSubtitle = "Daily milk record",
            navHome = "Home",
            navRecords = "Records",
            navSummary = "Summary",
            greetingMorning = "Good morning",
            greetingAfternoon = "Good afternoon",
            greetingEvening = "Good evening",
            today = "Today",
            todayDatePrefix = "Today:",
            todayMilk = "Today's milk",
            todayMilkMissing = "No milk record for today yet.",
            rateLabel = "Rate",
            todayTotal = "Today's total",
            addTodayMilk = "Add today's milk",
            editRecord = "Edit",
            thisMonth = "This month",
            totalMilk = "Total milk",
            totalAmount = "Total amount",
            recordedDays = "Days recorded",
            daysUnit = "days",
            daysUnitSingular = "day",
            viewRecords = "View records",
            monthlySummary = "Monthly summary",
            averageDailyMilk = "Daily average",
            moreOptions = "More options",
            addTitle = "Add milk",
            editTitle = "Edit record",
            dateLabel = "Date",
            changeDate = "Change date",
            quantityLabel = "Quantity",
            quantityHint = "e.g. 3, 2.5, 1.5",
            litres = "L",
            litreShort = "L",
            rateFieldLabel = "Rate per litre",
            rateHint = "e.g. 70",
            rupeesUnit = "Rs",
            amountLabel = "Total amount",
            amountAutoNote = "Calculated automatically.",
            saveRecord = "Save record",
            cancel = "Cancel",
            quickQuantityTitle = "Quick quantity",
            pickDate = "Choose date",
            dateDone = "OK",
            deleteRecord = "Delete record",
            recordsTitle = "History",
            recordsEmpty = "No milk records yet.",
            firstRecord = "Add first record",
            yesterday = "Yesterday",
            summaryTitle = "Summary",
            thisMonthTitle = "This month",
            previousMonth = "Previous month",
            nextMonth = "Next month",
            dailyRecords = "Daily records",
            monthEmpty = "No records this month.",
            grandTotal = "Month total",
            exportMonthReport = "Export monthly report (CSV)",
            duplicateTitle = "Record already exists",
            duplicateMessage = "A record for this date already exists. " +
                "Would you like to edit the existing record?",
            duplicateEdit = "Edit",
            deleteTitle = "Delete record?",
            deleteMessage = "This record will be deleted.",
            confirmDelete = "Delete",
            msgSaved = "Record saved.",
            msgUpdated = "Record updated.",
            msgDeleted = "Record deleted.",
            msgBackupCreated = "Backup file created.",
            msgBackupRestored = "Records restored from backup.",
            msgMonthExported = "Monthly report file created.",
            errQuantityRequired = "Please enter the milk quantity.",
            errQuantityMustBePositive = "Quantity must be greater than 0.",
            errQuantityTooLarge = "Quantity is too large.",
            errRateRequired = "Please enter the milk rate.",
            errRateMustBePositive = "Rate must be greater than 0.",
            errRateTooLarge = "Rate is too large.",
            errInvalidNumber = "Please enter a valid number.",
            errSaveFailed = "Could not save the record. Please try again.",
            errDateInvalid = "Please enter a valid date.",
            errBackupCreate = "Could not create the backup.",
            errBackupRestore = "Could not read the backup. The file is not valid.",
            errExport = "Could not create the report.",
            errNothingToBackup = "There are no records to back up.",
            backupTitle = "Backup",
            backupCreate = "Create backup file",
            backupRestore = "Restore from backup",
            backupRestoreTitle = "Restore backup?",
            backupRestoreMessage = "All records will be replaced by the ones in " +
                "the file. Are you sure?",
            backupConfirm = "Restore",
            recordCountFormat = "(%d records)",
            settingsTitle = "Settings",
            sectionGeneral = "General",
            sectionAppearance = "Appearance",
            sectionData = "Data",
            sectionAbout = "About",
            language = "Language",
            languageNepali = "नेपाली",
            languageEnglish = "English",
            theme = "Theme",
            themeLight = "Light",
            themeDark = "Dark",
            themeSystem = "System default",
            appVersionFormat = "Version %s",
            offlineNote = "Works fully offline. No account, no internet.",
            cdEdit = "Edit",
            cdDelete = "Delete",
            cdBack = "Go back",
            cdAdd = "Add a new record",
            cdSettings = "Open settings",
            cdMoreMenu = "More options",
            cdExportMonth = "Export monthly report",
            currencyPrefix = "Rs.",
            months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ),
            weekdays = listOf(
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
            )
        )

        /** Every language the app supports - used by the settings screen. */
        val all: List<Strings> = listOf(NEPALI, ENGLISH)

        // ------------------------------------------------------------
        // Active language for code that has no Compose context
        // (ViewModels, domain helpers). The composition keeps it in
        // sync through [activate]; a wrong value can only affect a
        // snackbar message, never stored data.
        @Volatile
        private var active: Strings = NEPALI

        fun activate(language: Language) {
            active = if (language == Language.ENGLISH) ENGLISH else NEPALI
        }

        fun activate(strings: Strings) {
            active = strings
        }

        /** The catalogue for a [Language]. */
        fun of(language: Language): Strings =
            if (language == Language.ENGLISH) ENGLISH else NEPALI

        val current: Strings get() = active

        // ------------------------------------------------------------
        // Convenience accessors for the default (Nepali) language.
        // Pure-Kotlin callers and the existing unit tests use these.
        val errQuantityRequired: String get() = NEPALI.errQuantityRequired
        val errQuantityMustBePositive: String get() = NEPALI.errQuantityMustBePositive
        val errInvalidNumber: String get() = NEPALI.errInvalidNumber
        val errRateRequired: String get() = NEPALI.errRateRequired
        val errRateMustBePositive: String get() = NEPALI.errRateMustBePositive
    }
}
