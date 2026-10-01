package com.milkhisab.app.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkhisab.app.data.backup.BackupFormatException
import com.milkhisab.app.data.backup.BackupManager
import com.milkhisab.app.data.backup.CsvExporter
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.data.repository.MilkRepository
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.utils.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.YearMonth

/**
 * Local backup (JSON) and restore.
 *
 * Restore always asks for confirmation first and validates the whole file
 * before touching the database; the actual replacement runs inside one Room
 * transaction, so a bad file can never wipe the family's records.
 */
class BackupViewModel(
    private val repository: MilkRepository
) : ViewModel() {

    data class UiState(
        val working: Boolean = false,
        /** True while the "are you sure?" restore dialog must be visible. */
        val confirmRestore: Boolean = false,
        /** Parsed-and-validated backup, waiting for user confirmation. */
        val pendingRecords: List<MilkRecordEntity> = emptyList(),
        val hasAnyRecord: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch {
            val count = repository.countRecords()
            _state.value = _state.value.copy(hasAnyRecord = count > 0)
        }
    }

    // ---------------------------------------------------------------- export

    /**
     * Writes every record as JSON to [uri]. All file work happens on
     * Dispatchers.IO; the UI thread is never blocked.
     */
    fun exportBackup(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val records = repository.getAllRecords()
                if (records.isEmpty()) {
                    emit(Strings.current.errNothingToBackup)
                    return@launch
                }
                val json = BackupManager.exportToJson(records)
                withContext(Dispatchers.IO) {
                    // "wt" truncates, so re-backing-up over an old file is safe.
                    val stream = resolver.openOutputStream(uri, "wt")
                        ?: throw IOException("Cannot open output")
                    stream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
                emit(Strings.current.msgBackupCreated)
            } catch (e: Exception) {
                emit(Strings.current.errBackupCreate)
            }
        }
    }

    // ---------------------------------------------------------------- import

    /**
     * Step 1 of restore: read + validate. If everything checks out, the
     * confirmation dialog is raised. Nothing is written at this point.
     */
    fun importBackup(uri: Uri, resolver: ContentResolver) {
        viewModelScope.launch {
            _state.value = _state.value.copy(working = true)
            val json = try {
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { input ->
                        input.readBytes().toString(Charsets.UTF_8)
                    }
                }
            } catch (e: Exception) {
                null
            }
            prepareRestore(json)
        }
    }

    /** Validation step, also usable directly (unit tests / future imports). */
    fun prepareRestore(json: String?) {
        if (json.isNullOrBlank()) {
            _state.value = _state.value.copy(working = false)
            viewModelScope.launch { emit(Strings.current.errBackupRestore) }
            return
        }
        _state.value = _state.value.copy(working = true)
        viewModelScope.launch {
            try {
                val records = BackupManager.importFromJson(json)
                _state.value = _state.value.copy(
                    working = false,
                    confirmRestore = true,
                    pendingRecords = records
                )
            } catch (e: BackupFormatException) {
                _state.value = _state.value.copy(working = false, pendingRecords = emptyList())
                emit(Strings.current.errBackupRestore)
            } catch (e: Exception) {
                _state.value = _state.value.copy(working = false, pendingRecords = emptyList())
                emit(Strings.current.errBackupRestore)
            }
        }
    }

    /** Step 2: the user tapped "फिर्ता ल्याउनुहोस्". */
    fun confirmRestore() {
        val records = _state.value.pendingRecords
        if (records.isEmpty()) {
            cancelRestore()
            return
        }
        _state.value = _state.value.copy(working = true, confirmRestore = false)
        viewModelScope.launch {
            try {
                repository.restoreAll(records)
                _state.value = UiState(hasAnyRecord = true)
                emit(Strings.current.msgBackupRestored)
            } catch (e: Exception) {
                _state.value = UiState(hasAnyRecord = true)
                emit(Strings.current.errBackupRestore)
            }
        }
    }

    fun cancelRestore() {
        _state.value = _state.value.copy(confirmRestore = false, pendingRecords = emptyList())
    }

    // ------------------------------------------------------------------ csv

    /**
     * Writes the monthly report (CSV) for [month] to [uri].
     * Shares the restore confirmation flow's state machine so the settings
     * screen only deals with one "working" flag.
     */
    fun exportMonthlyCsv(
        uri: Uri,
        resolver: ContentResolver,
        month: YearMonth,
        strings: Strings
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(working = true)
            try {
                val records = repository.getRecordsForMonth(month)
                if (records.isEmpty()) {
                    _state.value = _state.value.copy(working = false)
                    emit(strings.monthEmpty)
                    return@launch
                }
                val csv = CsvExporter.exportMonth(
                    Formatters.monthTitle(month, strings),
                    records
                )
                withContext(Dispatchers.IO) {
                    val stream = resolver.openOutputStream(uri, "wt")
                        ?: throw IOException("Cannot open output")
                    stream.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                }
                _state.value = _state.value.copy(working = false)
                emit(strings.msgMonthExported)
            } catch (e: Exception) {
                _state.value = _state.value.copy(working = false)
                emit(strings.errExport)
            }
        }
    }

    /** File name offered by the system "save as" dialog. */
    fun csvFileName(month: YearMonth): String = "milk-report-$month.csv"

    private suspend fun emit(text: String) {
        _events.send(UiEvent.Message(text))
    }
}
