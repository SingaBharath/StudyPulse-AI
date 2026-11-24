package com.bharath.studypulseai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary                = LightPrimary,
    onPrimary              = LightOnPrimary,
    primaryContainer       = LightPrimaryContainer,
    onPrimaryContainer     = LightOnPrimaryContainer,
    secondary              = LightSecondary,
    onSecondary            = LightOnSecondary,
    secondaryContainer     = LightSecondaryContainer,
    onSecondaryContainer   = LightOnSecondaryContainer,
    tertiary               = LightTertiary,
    onTertiary             = LightOnTertiary,
    tertiaryContainer      = LightTertiaryContainer,
    onTertiaryContainer    = LightOnTertiaryContainer,
    error                  = LightError,
    onError                = LightOnError,
    errorContainer         = LightErrorContainer,
    onErrorContainer       = LightOnErrorContainer,
    background             = LightBackground,
    onBackground           = LightOnBackground,
    surface                = LightSurface,
    onSurface              = LightOnSurface,
    surfaceVariant         = LightSurfaceVariant,
    onSurfaceVariant       = LightOnSurfaceVariant,
    outline                = LightOutline,
    outlineVariant         = LightOutlineVariant,
    inverseSurface         = LightInverseSurface,
    inverseOnSurface       = LightInverseOnSurface,
    surfaceTint            = LightSurfaceTint,
)

private val DarkColors = darkColorScheme(
    primary                = DarkPrimary,
    onPrimary              = DarkOnPrimary,
    primaryContainer       = DarkPrimaryContainer,
    onPrimaryContainer     = DarkOnPrimaryContainer,
    secondary              = DarkSecondary,
    onSecondary            = DarkOnSecondary,
    secondaryContainer     = DarkSecondaryContainer,
    onSecondaryContainer   = DarkOnSecondaryContainer,
    tertiary               = DarkTertiary,
    onTertiary             = DarkOnTertiary,
    tertiaryContainer      = DarkTertiaryContainer,
    onTertiaryContainer    = DarkOnTertiaryContainer,
    error                  = DarkError,
    onError                = DarkOnError,
    errorContainer         = DarkErrorContainer,
    onErrorContainer       = DarkOnErrorContainer,
    background             = DarkBackground,
    onBackground           = DarkOnBackground,
    surface                = DarkSurface,
    onSurface              = DarkOnSurface,
    surfaceVariant         = DarkSurfaceVariant,
    onSurfaceVariant       = DarkOnSurfaceVariant,
    outline                = DarkOutline,
    outlineVariant         = DarkOutlineVariant,
    inverseSurface         = DarkInverseSurface,
    inverseOnSurface       = DarkInverseOnSurface,
    surfaceTint            = DarkSurfaceTint,
)

@Composable
fun StudyPulseAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = StudyPulseTypography,
        shapes      = StudyPulseShapes,
        content     = content,
    )
}
