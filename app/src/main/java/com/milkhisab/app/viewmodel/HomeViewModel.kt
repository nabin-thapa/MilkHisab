package com.milkhisab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.data.repository.MilkRepository
import com.milkhisab.app.data.repository.MonthTotals
import com.milkhisab.app.utils.DateProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

/**
 * Home screen state: today's record plus the totals of the current month.
 *
 * Everything is a Flow straight from Room, so saving/editing/deleting a
 * record anywhere in the app immediately refreshes this screen.
 */
class HomeViewModel(
    private val repository: MilkRepository,
    private val dateProvider: DateProvider
) : ViewModel() {

    val today: LocalDate get() = dateProvider.today()

    private val currentMonth: YearMonth get() = YearMonth.from(today)

    val todayRecord: StateFlow<MilkRecordEntity?> =
        repository.observeRecordForDate(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val monthTotals: StateFlow<MonthTotals> =
        repository.observeMonthTotals(currentMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthTotals.EMPTY)
}
