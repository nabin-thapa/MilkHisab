package com.milkhisab.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.ui.components.Label
import com.milkhisab.app.ui.components.MilkCard
import com.milkhisab.app.ui.components.PrimaryButton
import com.milkhisab.app.ui.components.StatPair
import com.milkhisab.app.ui.components.ThinDivider
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.theme.Sizes
import com.milkhisab.app.ui.theme.Space
import com.milkhisab.app.utils.Formatters
import com.milkhisab.app.viewmodel.AppViewModelProvider
import com.milkhisab.app.viewmodel.HomeViewModel
import java.time.LocalTime

/**
 * गृह / Home - the screen opened every morning.
 *
 * Visual hierarchy, in order of importance:
 *   1. today's milk (the one thing that is time-critical)
 *   2. this month's totals
 *   settings sits in the top-right corner, deliberately out of the way.
 */
@Composable
fun HomeScreen(
    onAddToday: () -> Unit,
    onEditToday: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s = LocalStrings.current
    val todayRecord by viewModel.todayRecord.collectAsState()
    val monthTotals by viewModel.monthTotals.collectAsState()
    val today = viewModel.today
    // Read once per screen entry; the greeting only changes with the clock.
    val greeting = remember(today) { Formatters.greeting(LocalTime.now(), s) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.lg)
            .padding(top = Space.lg, bottom = Space.xxl),
        verticalArrangement = Arrangement.spacedBy(Space.xl)
    ) {
        // ------------------------------------------------------------ header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.appName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "$greeting · ${Formatters.dateFull(today, s)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SettingsButton(onClick = onOpenSettings)
        }

        // ------------------------------------------------------ today card
        TodayCard(
            record = todayRecord,
            onAddToday = onAddToday,
            onEditToday = onEditToday
        )

        // ------------------------------------------------------ this month
        Column {
            Label(
                text = s.thisMonth.uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Space.xs, bottom = Space.sm)
            )
            MilkCard {
                Column(modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.xs)) {
                    StatPair(
                        firstLabel = s.totalMilk,
                        firstValue = Formatters.litresWithUnit(monthTotals.totalMilk, s),
                        secondLabel = s.totalAmount,
                        secondValue = Formatters.money(monthTotals.totalAmount, s),
                        firstValueColor = MaterialTheme.colorScheme.onSurface,
                        secondValueColor = MaterialTheme.colorScheme.primary
                    )
                    ThinDivider()
                    StatPair(
                        firstLabel = s.recordedDays,
                        firstValue = Formatters.dayCount(monthTotals.recordedDays, s),
                        secondLabel = s.averageDailyMilk,
                        secondValue = Formatters.litresWithUnit(monthTotals.averageDailyMilk, s),
                        firstValueColor = MaterialTheme.colorScheme.onSurface,
                        secondValueColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** The top-right settings entry point - small, but a full 48dp target. */
@Composable
private fun SettingsButton(onClick: () -> Unit) {
    val s = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.size(Sizes.iconButtonSize)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = s.cdSettings,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Today's record. Tinted with the brand colour so it is obviously the
 * most important thing on the screen, in both light and dark mode.
 */
@Composable
private fun TodayCard(
    record: MilkRecordEntity?,
    onAddToday: () -> Unit,
    onEditToday: (Long) -> Unit
) {
    val s = LocalStrings.current
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer

    MilkCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        Column(modifier = Modifier.padding(Space.xl)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = s.todayMilk,
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainer,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = s.today,
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainer
                )
            }

            Spacer(Modifier.height(Space.lg))

            if (record == null) {
                Text(
                    text = s.todayMilkMissing,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onContainer
                )
                Spacer(Modifier.height(Space.xl))
                PrimaryButton(
                    text = s.addTodayMilk,
                    onClick = onAddToday,
                    icon = Icons.Filled.Add
                )
            } else {
                // The headline number: how much milk was recorded today.
                Text(
                    text = Formatters.litresWithUnit(record.quantity, s),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = onContainer
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = Formatters.quantityTimesRate(record.quantity, record.rate, s),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer
                )

                Spacer(Modifier.height(Space.xl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.lg)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Label(text = s.rateLabel, color = onContainer)
                        Spacer(Modifier.height(Space.xs))
                        Text(
                            text = Formatters.ratePerLitre(record.rate, s),
                            style = MaterialTheme.typography.titleMedium,
                            color = onContainer
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Label(text = s.todayTotal, color = onContainer)
                        Spacer(Modifier.height(Space.xs))
                        Text(
                            text = Formatters.money(record.amount, s),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = onContainer
                        )
                    }
                }

                Spacer(Modifier.height(Space.sm))

                // A small, secondary action: editing is rare, adding is not.
                TextButton(
                    onClick = { onEditToday(record.id) },
                    modifier = Modifier.height(Sizes.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = onContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = s.editRecord,
                        style = MaterialTheme.typography.labelLarge,
                        color = onContainer
                    )
                }
            }
        }
    }
}
