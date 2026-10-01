package com.milkhisab.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.milkhisab.app.ui.theme.Sizes
import com.milkhisab.app.ui.theme.LocalMilkSurfaces
import com.milkhisab.app.ui.theme.Space

/**
 * The shared building blocks.
 *
 * Rules the whole app follows, so the screens stay consistent:
 *  - cards use a hairline border instead of a heavy shadow;
 *  - numbers are the loudest thing on a card, labels stay quiet;
 *  - the primary action of a screen is a full-width 56dp button;
 *  - nothing tappable is smaller than 48dp.
 */

/** A quiet heading above a group of content. */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = modifier
    )
}

/** Small uppercase-style label used inside cards and on settings rows. */
@Composable
fun Label(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
    )
}

/**
 * The standard card: flat surface, hairline outline, soft corners.
 * Deliberately shadow-free so dark mode stays calm.
 */
@Composable
fun MilkCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, LocalMilkSurfaces.current.subtleBorder),
        content = content
    )
}

/** A card that sits one step back from [MilkCard] - used for grouped rows. */
@Composable
fun TonalCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    MilkCard(
        modifier = modifier,
        containerColor = LocalMilkSurfaces.current.muted,
        content = content
    )
}

/** Hairline separator that follows the theme. */
@Composable
fun ThinDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        thickness = 1.dp,
        color = LocalMilkSurfaces.current.subtleBorder
    )
}

/**
 * "label + value" - the number is deliberately the loudest element.
 */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Space.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

/** Two [StatTile]s separated by a vertical rule. */
@Composable
fun StatPair(
    modifier: Modifier = Modifier,
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    firstValueColor: Color = MaterialTheme.colorScheme.onSurface,
    secondValueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatTile(
            label = firstLabel,
            value = firstValue,
            valueColor = firstValueColor,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Space.sm)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(LocalMilkSurfaces.current.subtleBorder)
        )
        StatTile(
            label = secondLabel,
            value = secondValue,
            valueColor = secondValueColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Space.lg, vertical = Space.sm)
        )
    }
}

/** The one strong button of a screen: full width, 56dp, rounded. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.buttonHeight),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = Space.xl
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, Modifier.size(22.dp))
            Spacer(Modifier.width(Space.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

/** A calm alternative to [PrimaryButton]. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = Sizes.minTouchTarget),
        shape = RoundedCornerShape(14.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = LocalMilkSurfaces.current.muted,
            contentColor = contentColor
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = Space.lg
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, Modifier.size(20.dp))
            Spacer(Modifier.width(Space.sm))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Row of two equally weighted buttons. */
@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        content()
    }
}

/**
 * Empty states: one icon, one sentence, one clear way out.
 * Never an illustration - the point is to say what to do next.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Space.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = LocalMilkSurfaces.current.muted,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(Space.lg))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(Space.xl))
            PrimaryButton(
                text = actionText,
                onClick = onAction,
                icon = Icons.Filled.Add
            )
        }
    }
}

/** Settings / menu row: icon, title, optional subtitle, trailing slot. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier.selectable(selected = false, onClick = onClick)
            } else {
                Modifier
            }
        )
        .heightIn(min = Sizes.minTouchTarget)
        .padding(vertical = Space.md, horizontal = Space.lg)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = LocalMilkSurfaces.current.elevated,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.width(Space.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(Space.sm))
            trailing()
        }
    }
}

/**
 * A single-choice row (language, theme). Selected state is obvious from
 * the radio button *and* the tinted surface, so it is readable at a glance
 * and still announced correctly by TalkBack.
 */
@Composable
fun ChoiceRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val container = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val onContainer = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalMilkSurfaces.current.subtleBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.minTouchTarget)
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = onContainer,
                modifier = Modifier.weight(1f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Space.sm))
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

/** Section label used on the settings screen (GENERAL / DATA / ...). */
@Composable
fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = Space.lg, top = Space.xl, bottom = Space.sm)
    )
}

/** A text button that spans the width - used for "escape" actions. */
@Composable
fun WideTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.minTouchTarget)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

/** Used when a decorative element should be skipped by screen readers. */
fun Modifier.decorative(): Modifier = clearAndSetSemantics { }
