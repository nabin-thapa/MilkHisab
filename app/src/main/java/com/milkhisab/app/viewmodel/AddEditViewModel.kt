package com.milkhisab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.data.repository.DuplicateRecordException
import com.milkhisab.app.data.repository.MilkRepository
import com.milkhisab.app.domain.MilkCalculator
import com.milkhisab.app.domain.Validators
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.utils.DateProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Add / Edit screen state.
 *
 * The amount is NEVER typed by the user - it is recomputed on every
 * keystroke from quantity x rate, so it can never disagree with the
 * numbers on screen.
 */
data class AddEditState(
    /** null = adding a new record; non-null = editing an existing one. */
    val recordId: Long? = null,
    val date: LocalDate,
    val quantityText: String = "",
    val rateText: String = "",
    /** Live preview of quantity x rate (0 until both fields are valid). */
    val amount: Double = 0.0,
    val quantityError: String? = null,
    val rateError: String? = null,
    val dateError: String? = null,
    /** Duplicate-date dialog visibility. */
    val showDuplicateDialog: Boolean = false,
    /** Existing record id for the date the user picked (duplicate case). */
    val conflictingRecordId: Long? = null,
    val showDeleteDialog: Boolean = false,
    val isSaving: Boolean = false,
    val prefillReady: Boolean = false,
    /** Explains why the date cannot be changed while editing (if locked). */
    val isEdit: Boolean = false
)

sealed interface AddEditEvent {
    data class Message(val text: String) : AddEditEvent
    data object Saved : AddEditEvent
    data object Close : AddEditEvent
}

/**
 * Drives both the "दूध थप्नुहोस्" (add) and "सम्पादन" (edit) screens.
 *
 * Behaviour highlights:
 *  - new records prefill the rate with the most recent rate used;
 *  - amount updates instantly while typing;
 *  - saving checks for an existing record on the same date and asks the
 *    user instead of silently duplicating the day.
 */
class AddEditViewModel(
    private val repository: MilkRepository,
    private val dateProvider: DateProvider,
    /** Existing record id when editing, null when adding. */
    initialRecordId: Long? = null,
    /** Date to open with (defaults to today when adding). */
    initialDate: LocalDate? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddEditState(
            recordId = initialRecordId,
            date = initialDate ?: dateProvider.today(),
            isEdit = initialRecordId != null
        )
    )
    val state: StateFlow<AddEditState> = _state.asStateFlow()

    private val _events = Channel<AddEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                if (initialRecordId != null) {
                    val existing = repository.getRecordById(initialRecordId)
                    if (existing != null) {
                        _state.update {
                            it.copy(
                                date = existing.date,
                                quantityText = formatInput(existing.quantity),
                                rateText = formatInput(existing.rate),
                                amount = existing.amount,
                                prefillReady = true
                            )
                        }
                    }
                } else {
                    // Prefill with the most recently used rate.
                    val latest = repository.getLatestRate()
                    if (latest != null) {
                        _state.update { it.copy(rateText = formatInput(latest)) }
                    }
                    recalc()
                    _state.update { it.copy(prefillReady = true) }
                }
                recalc()
            } catch (e: Exception) {
                _state.update { it.copy(prefillReady = true) }
            }
        }
    }

    // ------------------------------------------------------------- user input

    fun onQuantityChanged(text: String) {
        _state.update { it.copy(quantityText = text, quantityError = null) }
        recalc()
    }

    fun onRateChanged(text: String) {
        _state.update { it.copy(rateText = text, rateError = null) }
        recalc()
    }

    /** Quick buttons: 1 L, 1.5 L, 2 L ... (manual typing still allowed). */
    fun onQuickQuantity(value: Double) {
        _state.update { it.copy(quantityText = formatInput(value), quantityError = null) }
        recalc()
    }

    fun onDatePicked(date: LocalDate) {
        _state.update { it.copy(date = date, dateError = null) }
        recalc()
    }

    /** Recomputes the live total after any input change. */
    private fun recalc() {
        val s = _state.value
        val q = Validators.parseNumber(s.quantityText)
        val r = Validators.parseNumber(s.rateText)
        val amount = if (q != null && r != null) {
            MilkCalculator.calculateAmount(q, r)
        } else {
            0.0
        }
        _state.update { it.copy(amount = amount) }
    }

    // ------------------------------------------------------------------ save

    fun onSaveClicked() {
        val s = _state.value
        val (qError, rError) = Validators.validateForm(
            s.quantityText,
            s.rateText,
            Strings.current
        )
        if (qError != null || rError != null) {
            _state.update { it.copy(quantityError = qError, rateError = rError) }
            return
        }

        val quantity = Validators.parseNumber(s.quantityText) ?: return
        val rate = Validators.parseNumber(s.rateText) ?: return
        val amount = MilkCalculator.calculateAmount(quantity, rate)

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                if (s.recordId == null) {
                    // --- adding a new day -----------------------------------
                    val existing = repository.getRecordForDate(s.date)
                    if (existing != null) {
                        // Do not silently duplicate: ask the user.
                        _state.update {
                            it.copy(
                                isSaving = false,
                                showDuplicateDialog = true,
                                conflictingRecordId = existing.id
                            )
                        }
                        return@launch
                    }
                    repository.insertRecord(
                        MilkRecordEntity(
                            date = s.date,
                            quantity = quantity,
                            rate = rate,
                            amount = amount
                        )
                    )
                    emitMessage(Strings.current.msgSaved)
                    _events.send(AddEditEvent.Saved)
                } else {
                    // --- editing --------------------------------------------
                    val current = repository.getRecordById(s.recordId)
                        ?: run {
                            _state.update { it.copy(isSaving = false) }
                            emitMessage(Strings.current.errSaveFailed)
                            _events.send(AddEditEvent.Close)
                            return@launch
                        }
                    repository.updateRecord(
                        current.copy(
                            date = s.date,
                            quantity = quantity,
                            rate = rate,
                            amount = amount,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    emitMessage(Strings.current.msgUpdated)
                    _events.send(AddEditEvent.Saved)
                }
            } catch (e: DuplicateRecordException) {
                _state.update { it.copy(isSaving = false, showDuplicateDialog = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                emitMessage(Strings.current.errSaveFailed)
            }
        }
    }

    // -------------------------------------------------------- duplicate dialog

    /** "सम्पादन गर्नुहोस्" inside the duplicate dialog: edit the old record. */
    fun onDuplicateEdit() {
        val conflictId = _state.value.conflictingRecordId ?: return
        _state.update { it.copy(showDuplicateDialog = false, conflictingRecordId = null) }
        viewModelScope.launch {
            val existing = repository.getRecordById(conflictId)
            if (existing == null) {
                emitMessage(Strings.current.errSaveFailed)
                return@launch
            }
            _state.update {
                it.copy(
                    recordId = existing.id,
                    isEdit = true,
                    date = existing.date,
                    quantityText = formatInput(existing.quantity),
                    rateText = formatInput(existing.rate),
                    amount = existing.amount
                )
            }
            recalc()
        }
    }

    /** "रद्द गर्नुहोस्" inside the duplicate dialog. */
    fun onDuplicateDismiss() {
        _state.update { it.copy(showDuplicateDialog = false, conflictingRecordId = null) }
    }

    // ------------------------------------------------------------- delete

    fun onDeleteClicked() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismissed() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteConfirmed() {
        val id = _state.value.recordId ?: run {
            _state.update { it.copy(showDeleteDialog = false) }
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteRecordById(id)
                emitMessage(Strings.current.msgDeleted)
            } catch (e: Exception) {
                emitMessage(Strings.current.errSaveFailed)
            } finally {
                _state.update { it.copy(showDeleteDialog = false) }
                _events.send(AddEditEvent.Close)
            }
        }
    }

    fun onCloseRequested() {
        viewModelScope.launch { _events.send(AddEditEvent.Close) }
    }

    // --------------------------------------------------------------- helpers

    /** "3", "2.5", "70" - no trailing ".0" noise while typing. */
    private fun formatInput(value: Double): String {
        val asLong = value.toLong()
        return if (value == asLong.toDouble()) asLong.toString() else value.toString()
    }

    private suspend fun emitMessage(text: String) {
        _events.send(AddEditEvent.Message(text))
    }
}
