package com.milkhisab.app.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.ui.components.EmptyState
import com.milkhisab.app.ui.components.RecordCard
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.theme.Space
import com.milkhisab.app.utils.Formatters
import com.milkhisab.app.viewmodel.AppViewModelProvider
import com.milkhisab.app.viewmodel.RecordsViewModel
import com.milkhisab.app.viewmodel.UiEvent

/**
 * पुराना रेकर्ड / History - every record, newest first.
 *
 * One card per day, scannable without reading: date, amount, and the two
 * actions on the right. An always-visible "+" in the top bar means a new
 * day can be recorded from here too, not only from Home.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onAddRecord: () -> Unit,
    onEditRecord: (Long) -> Unit,
    onMessage: (String) -> Unit,
    viewModel: RecordsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s = LocalStrings.current
    val records by viewModel.records.collectAsState()
    val today = viewModel.today
    var pendingDelete by remember { mutableStateOf<MilkRecordEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Message -> onMessage(event.text)
                is UiEvent.ConfirmDelete -> pendingDelete = event.record
                is UiEvent.Error -> onMessage(event.text)
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = s.recordsTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onAddRecord) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = s.cdAdd
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (records.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.List,
                message = s.recordsEmpty,
                actionText = s.firstRecord,
                onAction = onAddRecord,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = Space.lg,
                    vertical = Space.sm
                ),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                items(records, key = { it.id }) { record ->
                    RecordCard(
                        record = record,
                        dateLabel = Formatters.dateShort(record.date, today, s),
                        onEdit = { onEditRecord(record.id) },
                        onDelete = { viewModel.onDeleteClicked(record) }
                    )
                }
            }
        }
    }

    // ------------------------------------------------- delete confirmation
    pendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(s.deleteTitle) },
            text = {
                Text(
                    "${s.deleteMessage}\n\n" +
                        "${Formatters.dateFull(record.date, s)} · " +
                        Formatters.litresWithUnit(record.quantity, s)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteConfirmed(record)
                        pendingDelete = null
                    }
                ) {
                    Text(s.confirmDelete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(s.cancel)
                }
            }
        )
    }
}
