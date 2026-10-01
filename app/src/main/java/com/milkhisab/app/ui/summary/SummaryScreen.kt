package com.milkhisab.app.ui.summary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milkhisab.app.ui.components.EmptyState
import com.milkhisab.app.ui.components.Label
import com.milkhisab.app.ui.components.MilkCard
import com.milkhisab.app.ui.components.RecordDivider
import com.milkhisab.app.ui.components.RecordLine
import com.milkhisab.app.ui.components.StatPair
import com.milkhisab.app.ui.components.ThinDivider
import com.milkhisab.app.ui.components.WideTextButton
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.theme.Sizes
import com.milkhisab.app.ui.theme.Space
import com.milkhisab.app.utils.Formatters
import com.milkhisab.app.viewmodel.AppViewModelProvider
import com.milkhisab.app.viewmodel.SummaryViewModel
import java.io.IOException

/**
 * हिसाब / Summary - the month at a glance.
 *
 * Numbers first, then the days that produced them. No charts: a parent
 * wants the total, the average and the daily list, in that order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onMessage: (String) -> Unit,
    onOpenRecords: () -> Unit,
    viewModel: SummaryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s = LocalStrings.current
    val month by viewModel.selectedMonth.collectAsState()
    val records by viewModel.records.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val context = LocalContext.current

    // Save-As picker for the monthly CSV report (no storage permission needed).
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csv = viewModel.buildMonthlyReport(s)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                } ?: throw IOException("no stream")
                onMessage(s.msgMonthExported)
            } catch (e: Exception) {
                onMessage(s.errExport)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(s.summaryTitle, style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (records.isEmpty()) {
                                onMessage(s.monthEmpty)
                            } else {
                                exportLauncher.launch(viewModel.reportFileName())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.IosShare,
                            contentDescription = s.cdExportMonth
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Space.lg,
                vertical = Space.sm
            ),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            // ------------------------------------------------- month picker
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = viewModel::previousMonth,
                        modifier = Modifier.size(Sizes.iconButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = s.previousMonth,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = Formatters.monthTitle(month, s),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Space.sm)
                    )
                    IconButton(
                        onClick = viewModel::nextMonth,
                        modifier = Modifier.size(Sizes.iconButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = s.nextMonth,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ----------------------------------------------------- totals
            item {
                MilkCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = Space.lg,
                            vertical = Space.xs
                        )
                    ) {
                        StatPair(
                            firstLabel = s.totalMilk,
                            firstValue = Formatters.litresWithUnit(totals.totalMilk, s),
                            secondLabel = s.totalAmount,
                            secondValue = Formatters.money(totals.totalAmount, s),
                            secondValueColor = MaterialTheme.colorScheme.primary
                        )
                        ThinDivider()
                        StatPair(
                            firstLabel = s.recordedDays,
                            firstValue = Formatters.dayCount(totals.recordedDays, s),
                            secondLabel = s.averageDailyMilk,
                            secondValue = Formatters.litresWithUnit(totals.averageDailyMilk, s)
                        )
                    }
                }
            }

            // ------------------------------------------------ daily records
            item {
                Label(
                    text = s.dailyRecords.uppercase(),
                    modifier = Modifier.padding(start = Space.xs)
                )
            }

            if (records.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.EventNote,
                        message = s.monthEmpty,
                        modifier = Modifier.height(220.dp)
                    )
                }
            } else {
                item {
                    // One grouped card with hairline separators: tidier than
                    // a stack of separate cards.
                    MilkCard {
                        Column {
                            records.forEachIndexed { index, record ->
                                RecordLine(
                                    dateLabel = Formatters.dateDayMonth(record.date, s),
                                    detail = Formatters.quantityTimesRate(
                                        record.quantity,
                                        record.rate,
                                        s
                                    ),
                                    amount = Formatters.money(record.amount, s),
                                    strings = s
                                )
                                if (index != records.lastIndex) RecordDivider()
                            }
                        }
                    }
                }

                // ------------------------------------------------ grand total
                item {
                    MilkCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Column(modifier = Modifier.padding(Space.lg)) {
                            Text(
                                text = s.grandTotal,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(Space.md))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = s.totalMilk,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = Formatters.litresWithUnit(totals.totalMilk, s),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.height(Space.sm))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = s.totalAmount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = Formatters.money(totals.totalAmount, s),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // ---------------------------------------- jump to full history
                item {
                    WideTextButton(text = s.viewRecords, onClick = onOpenRecords)
                }
            }
        }
    }
}
