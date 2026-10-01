package com.milkhisab.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.milkhisab.app.data.local.MilkRecordEntity
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.strings.Strings
import com.milkhisab.app.ui.theme.Sizes
import com.milkhisab.app.ui.theme.LocalMilkSurfaces
import com.milkhisab.app.ui.theme.Space
import com.milkhisab.app.utils.Formatters

/**
 * One row in the history list.
 *
 * Layout intent: the date is what the user scans for, the amount is what
 * they care about, and the actions stay in the corner but remain big
 * enough to hit:
 *
 *   आज                             Rs. 210
 *   3.0 L × Rs.70                 [edit] [delete]
 */
@Composable
fun RecordCard(
    record: MilkRecordEntity,
    dateLabel: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    strings: Strings = LocalStrings.current
) {
    MilkCard(modifier = modifier) {
        Column(modifier = Modifier.padding(start = Space.lg, end = Space.sm, top = Space.md, bottom = Space.sm)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Formatters.money(record.amount, strings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.quantityTimesRate(record.quantity, record.rate, strings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Space.sm))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(Sizes.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = strings.cdEdit,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(Sizes.iconButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = strings.cdDelete,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Read-only line used in the monthly summary's day-by-day list, where
 * edit/delete would be noise.
 */
@Composable
fun RecordLine(
    dateLabel: String,
    detail: String,
    amount: String,
    modifier: Modifier = Modifier,
    strings: Strings = LocalStrings.current
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.minTouchTarget)
            .padding(horizontal = Space.lg, vertical = Space.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** Divider used between record lines inside a grouped card. */
@Composable
fun RecordDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = Space.lg),
        thickness = 1.dp,
        color = LocalMilkSurfaces.current.subtleBorder
    )
}
