package com.milkhisab.app.ui.addedit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milkhisab.app.ui.components.Label
import com.milkhisab.app.ui.components.MilkCard
import com.milkhisab.app.ui.components.PrimaryButton
import com.milkhisab.app.ui.components.ThinDivider
import com.milkhisab.app.ui.components.WideTextButton
import com.milkhisab.app.domain.Validators
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.theme.Sizes
import com.milkhisab.app.ui.theme.Space
import com.milkhisab.app.utils.Formatters
import com.milkhisab.app.viewmodel.AddEditEvent
import com.milkhisab.app.viewmodel.AddEditViewModel
import com.milkhisab.app.viewmodel.AppViewModelProvider
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * दूध थप्नुहोस् / Edit record.
 *
 * Three inputs (date, quantity, rate) and one result. The amount is never
 * typed - it is shown in a tinted card that always explains where it came
 * from ("3.0 L × Rs.70").
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    recordId: Long?,
    onDone: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: AddEditViewModel = viewModel(
        key = "add_edit_${recordId ?: -1L}",
        factory = AppViewModelProvider.factoryForAddEdit(recordId)
    )
) {
    val s = LocalStrings.current
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val rateFocus = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Forward one-shot events: snackbar + close.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditEvent.Message -> onMessage(event.text)
                AddEditEvent.Saved -> onDone()
                AddEditEvent.Close -> onDone()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEdit) s.editTitle else s.addTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onCloseRequested() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.cdBack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = Space.lg, vertical = Space.sm),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            // --------------------------------------------------------- date
            Column {
                Label(
                    text = s.dateLabel,
                    modifier = Modifier.padding(start = Space.xs, bottom = Space.sm)
                )
                DateField(
                    value = Formatters.dateWithWeekday(state.date, s),
                    onClick = { showDatePicker = true },
                    isError = state.dateError != null
                )
                if (state.dateError != null) {
                    Text(
                        text = state.dateError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = Space.sm, top = Space.xs)
                    )
                }
            }

            // ---------------------------------------------------- quantity
            OutlinedTextField(
                value = state.quantityText,
                onValueChange = viewModel::onQuantityChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(s.quantityLabel) },
                placeholder = { Text(s.quantityHint) },
                suffix = { Text(s.litres) },
                singleLine = true,
                isError = state.quantityError != null,
                supportingText = state.quantityError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { rateFocus.requestFocus() }
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Quick quantity chips - a shortcut, typing still works.
            Column {
                Label(
                    text = s.quickQuantityTitle,
                    modifier = Modifier.padding(start = Space.xs, bottom = Space.sm)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    listOf(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0).forEach { value ->
                        val selected =
                            viewModel.state.value.quantityText.toDoubleOrNull() == value
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onQuickQuantity(value) },
                            label = {
                                Text(Formatters.litresWithUnit(value, s))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor =
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // --------------------------------------------------------- rate
            OutlinedTextField(
                value = state.rateText,
                onValueChange = viewModel::onRateChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(rateFocus),
                label = { Text(s.rateFieldLabel) },
                placeholder = { Text(s.rateHint) },
                prefix = { Text(s.currencyPrefix) },
                singleLine = true,
                isError = state.rateError != null,
                supportingText = state.rateError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onSaveClicked()
                    }
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // ------------------------------------------------- live total
            MilkCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = s.amountLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        text = Formatters.money(state.amount, s),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (state.quantityText.isNotBlank() && state.rateText.isNotBlank()) {
                        Spacer(Modifier.height(Space.md))
                        ThinDivider()
                        Spacer(Modifier.height(Space.md))
                        Text(
                            text = Formatters.quantityTimesRate(
                                Validators.parseNumber(state.quantityText) ?: 0.0,
                                Validators.parseNumber(state.rateText) ?: 0.0,
                                s
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // -------------------------------------------------------- save
            PrimaryButton(
                text = s.saveRecord,
                onClick = { viewModel.onSaveClicked() },
                enabled = !state.isSaving
            )

            // ------------------------------------------------------ delete
            if (state.isEdit) {
                WideTextButton(
                    text = s.deleteRecord,
                    onClick = { viewModel.onDeleteClicked() },
                    contentColor = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(Space.sm))
        }
    }

    // ------------------------------------------------------- date picker
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.onDatePicked(picked)
                    }
                    showDatePicker = false
                }) {
                    Text(s.dateDone)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(s.cancel)
                }
            }
        ) {
            // The dialog's own title is a system string, so we render our
            // own heading to keep the picker in the chosen language.
            Column {
                Text(
                    text = s.pickDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        start = Space.xl,
                        end = Space.xl,
                        top = Space.xl,
                        bottom = Space.sm
                    )
                )
                DatePicker(state = pickerState)
            }
        }
    }

    // ------------------------------------------------- duplicate date dialog
    if (state.showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDuplicateDismiss,
            title = { Text(s.duplicateTitle) },
            text = { Text(s.duplicateMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::onDuplicateEdit) {
                    Text(s.duplicateEdit)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDuplicateDismiss) {
                    Text(s.cancel)
                }
            }
        )
    }

    // ------------------------------------------------------ delete dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismissed,
            title = { Text(s.deleteTitle) },
            text = { Text(s.deleteMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirmed) {
                    Text(
                        text = s.confirmDelete,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismissed) {
                    Text(s.cancel)
                }
            }
        )
    }
}

/**
 * Read-only date field. Looks like an input, opens the picker on tap, and
 * is at least 56dp tall so it is easy to hit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    value: String,
    onClick: () -> Unit,
    isError: Boolean
) {
    val s = LocalStrings.current
    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.buttonHeight)
            .semantics { contentDescription = "${s.dateLabel}: $value" },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = s.changeDate,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
