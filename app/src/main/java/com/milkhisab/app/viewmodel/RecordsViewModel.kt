package com.milkhisab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.data.repository.DuplicateRecordException
import com.milkhisab.app.data.repository.MilkRepository
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.utils.DateProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One-shot messages shown as Snackbars ("रेकर्ड सेभ भयो।" etc.).
 */
sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data class ConfirmDelete(val record: MilkRecordEntity) : UiEvent
    data object Deleted : UiEvent
    data object DuplicateDate : UiEvent
    data class Error(val text: String) : UiEvent
}

/**
 * "पुराना रेकर्ड" screen: full history, edit and delete.
 */
class RecordsViewModel(
    private val repository: MilkRepository,
    private val dateProvider: DateProvider
) : ViewModel() {

    val today = dateProvider.today()

    val records: StateFlow<List<MilkRecordEntity>> =
        repository.observeAllRecords()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Deleting always goes through a confirmation dialog first. */
    fun onDeleteClicked(record: MilkRecordEntity) {
        viewModelScope.launch {
            _events.send(UiEvent.ConfirmDelete(record))
        }
    }

    fun onDeleteConfirmed(record: MilkRecordEntity) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(record)
                _events.send(UiEvent.Message(Strings.current.msgDeleted))
            } catch (e: Exception) {
                _events.send(
                    UiEvent.Error(
                        Strings.current.errSaveFailed
                    )
                )
            }
        }
    }
}
