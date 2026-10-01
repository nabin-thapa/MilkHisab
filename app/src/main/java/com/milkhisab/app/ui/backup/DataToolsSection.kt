package com.milkhisab.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milkhisab.app.ui.components.MilkCard
import com.milkhisab.app.ui.components.SettingsRow
import com.milkhisab.app.ui.components.ThinDivider
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.utils.DateProvider
import com.milkhisab.app.viewmodel.AppViewModelProvider
import com.milkhisab.app.viewmodel.BackupViewModel
import com.milkhisab.app.viewmodel.UiEvent

/**
 * Backup / restore / CSV export, grouped in the Settings "Data" section.
 *
 * Nothing leaves the phone: a JSON backup is written wherever the user
 * picks, and a restore only runs after a confirmation dialog. No account,
 * no cloud, no INTERNET permission.
 */
@Composable
fun DataToolsSection(
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val dateProvider = remember { DateProvider() }
    val currentMonth = remember { dateProvider.monthNow() }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Message -> onMessage(event.text)
                else -> Unit
            }
        }
    }

    // ------------------------------------------------------- export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri, context.contentResolver)
        }
    }

    // ------------------------------------------------------- import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri, context.contentResolver)
        }
    }

    // ---------------------------------------------------------- csv launcher
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportMonthlyCsv(
                uri,
                context.contentResolver,
                currentMonth,
                s
            )
        }
    }

    MilkCard(modifier = modifier.fillMaxWidth()) {
        Column {
            SettingsRow(
                icon = Icons.Filled.FileDownload,
                title = s.backupCreate,
                onClick = { exportLauncher.launch("milk-record-backup.json") }
            )
            ThinDivider()
            SettingsRow(
                icon = Icons.Filled.Restore,
                title = s.backupRestore,
                onClick = {
                    importLauncher.launch(
                        arrayOf("application/json", "text/plain", "application/octet-stream")
                    )
                }
            )
            ThinDivider()
            SettingsRow(
                icon = Icons.Filled.TableChart,
                title = s.exportMonthReport,
                onClick = { csvLauncher.launch(viewModel.csvFileName(currentMonth)) }
            )

            if (state.working) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 12.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }

    // --------------------------------------------- restore confirmation dialog
    if (state.confirmRestore) {
        AlertDialog(
            onDismissRequest = viewModel::cancelRestore,
            title = { Text(s.backupRestoreTitle) },
            text = {
                Text(
                    s.backupRestoreMessage + "\n\n" +
                        String.format(
                            s.recordCountFormat,
                            state.pendingRecords.size
                        )
                )
            },
            confirmButton = {
                Button(onClick = viewModel::confirmRestore) {
                    Text(s.backupConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelRestore) {
                    Text(s.cancel)
                }
            }
        )
    }
}
