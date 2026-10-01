package com.milkhisab.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The one spacing scale used across every screen.
 *
 * Screens compose gaps out of these values instead of inventing padding,
 * which is what keeps the rhythm consistent when new pieces are added.
 */
object Space {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
}

/** Comfortable sizes for parents - nothing tappable is smaller than this. */
object Sizes {
    /** Minimum touch target for anything tappable. */
    val minTouchTarget: Dp = 48.dp
    /** Primary call-to-action height. */
    val buttonHeight: Dp = 56.dp
    val iconButtonSize: Dp = 48.dp
}
