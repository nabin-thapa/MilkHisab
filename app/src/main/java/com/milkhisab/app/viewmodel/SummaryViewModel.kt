package com.milkhisab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkhisab.app.data.backup.CsvExporter
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.data.repository.MilkRepository
import com.milkhisab.app.data.repository.MonthTotals
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.utils.DateProvider
import com.milkhisab.app.utils.Formatters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

/**
 * "हिसाब" (monthly summary) screen.
 *
 * The selected month is user-controlled; every value on screen derives from
 * that month only. Totals always sum each record's own amount, so mixed
 * rates inside one month are handled correctly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModel(
    private val repository: MilkRepository,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(dateProvider.monthNow())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val records: StateFlow<List<MilkRecordEntity>> =
        _selectedMonth
            .flatMapLatest { month -> repository.observeRecordsForMonth(month) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totals: StateFlow<MonthTotals> =
        _selectedMonth
            .flatMapLatest { month -> repository.observeMonthTotals(month) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthTotals.EMPTY)

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun goToCurrentMonth() {
        _selectedMonth.value = dateProvider.monthNow()
    }

    fun isCurrentMonth(): Boolean = _selectedMonth.value == dateProvider.monthNow()

    fun monthTitle(strings: Strings = Strings.NEPALI): String =
        Formatters.monthTitle(_selectedMonth.value, strings)

    /** Default file name for the monthly CSV report. */
    fun reportFileName(): String {
        val month = _selectedMonth.value
        return "milk-report-" +
            month.toString() + // "2026-10"
            ".csv"
    }

    /**
     * Builds the CSV text for the selected month. Pure string building,
     * so the caller only has to write it to a file.
     */
    fun buildMonthlyReport(strings: Strings = Strings.NEPALI): String {
        val month = _selectedMonth.value
        return CsvExporter.exportMonth(Formatters.monthTitle(month, strings), records.value)
    }
}
