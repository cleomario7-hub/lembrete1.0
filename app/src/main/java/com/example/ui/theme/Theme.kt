package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = OnTerracottaPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = SageSecondary,
    onSecondary = OnSageSecondary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = AmberTertiary,
    onTertiary = OnAmberTertiary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = CreamBackground,
    onBackground = OnCreamBackground,
    surface = IvorySurface,
    onSurface = OnIvorySurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = OnWarmSurfaceVariant,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
    error = ErrorCrimson,
    onError = OnErrorCrimson,
    errorContainer = ErrorCrimsonContainer,
    onErrorContainer = OnErrorCrimsonContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaDarkPrimary,
    onPrimary = OnTerracottaDarkPrimary,
    primaryContainer = TerracottaDarkContainer,
    onPrimaryContainer = OnTerracottaDarkContainer,
    secondary = SageDarkSecondary,
    onSecondary = OnSageDarkSecondary,
    secondaryContainer = SageDarkContainer,
    onSecondaryContainer = OnSageDarkContainer,
    tertiary = AmberDarkTertiary,
    onTertiary = OnAmberDarkTertiary,
    tertiaryContainer = AmberDarkContainer,
    onTertiaryContainer = OnAmberDarkContainer,
    background = EspressoBackground,
    onBackground = OnEspressoBackground,
    surface = WalnutSurface,
    onSurface = OnWalnutSurface,
    surfaceVariant = WalnutSurfaceVariant,
    onSurfaceVariant = OnWalnutSurfaceVariant,
    outline = EspressoOutline,
    outlineVariant = EspressoOutlineVariant,
    error = ErrorCrimsonContainer,
    onError = OnErrorCrimsonContainer,
    errorContainer = ErrorCrimson,
    onErrorContainer = OnErrorCrimson
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LembretesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
