package com.milkhisab.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A few surface tones the Material 3 colour scheme of the version we build
 * against does not expose yet.
 *
 * Having them here (instead of scattering `surfaceVariant.copy(...)` calls)
 * keeps the light/dark relationship in one readable place: a card sits on
 * [muted], small controls sit on [elevated], and both are one step quieter
 * than the main surface.
 */
@Immutable
data class MilkSurfaces(
    /** One step back from a card - grouped rows, quiet buttons. */
    val muted: Color,
    /** Slightly stronger - icon chips and small controls. */
    val elevated: Color,
    /** Hairline that must stay visible without drawing attention. */
    val subtleBorder: Color
)

internal val LightSurfaces = MilkSurfaces(
    muted = LightSurfaceContainer,
    elevated = LightSurfaceContainerHigh,
    subtleBorder = LightOutlineVariant
)

internal val DarkSurfaces = MilkSurfaces(
    muted = DarkSurfaceContainer,
    elevated = DarkSurfaceContainerHigh,
    subtleBorder = DarkOutlineVariant
)

val LocalMilkSurfaces = staticCompositionLocalOf { LightSurfaces }
