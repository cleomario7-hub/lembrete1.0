package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Palette - Warm Organic Editorial
val TerracottaPrimary = Color(0xFFC84B31)
val OnTerracottaPrimary = Color(0xFFFFFFFF)
val TerracottaContainer = Color(0xFFFFDBD2)
val OnTerracottaContainer = Color(0xFF400E03)

val SageSecondary = Color(0xFF2D6A4F)
val OnSageSecondary = Color(0xFFFFFFFF)
val SageContainer = Color(0xFFD1EFE0)
val OnSageContainer = Color(0xFF072B1B)

val AmberTertiary = Color(0xFFB45309)
val OnAmberTertiary = Color(0xFFFFFFFF)
val AmberContainer = Color(0xFFFDE68A)
val OnAmberContainer = Color(0xFF3B1800)

val CreamBackground = Color(0xFFFBF8F3)
val OnCreamBackground = Color(0xFF1E1916)
val IvorySurface = Color(0xFFFFFDF9)
val OnIvorySurface = Color(0xFF1E1916)
val WarmSurfaceVariant = Color(0xFFF2ECE4)
val OnWarmSurfaceVariant = Color(0xFF534841)
val WarmOutline = Color(0xFF86786E)
val WarmOutlineVariant = Color(0xFFD9CFC5)

val ErrorCrimson = Color(0xFFBA1A1A)
val OnErrorCrimson = Color(0xFFFFFFFF)
val ErrorCrimsonContainer = Color(0xFFFFDAD6)
val OnErrorCrimsonContainer = Color(0xFF410002)

// Dark Palette - Warm Espresso & Glowing Coral
val TerracottaDarkPrimary = Color(0xFFFF8A65)
val OnTerracottaDarkPrimary = Color(0xFF561404)
val TerracottaDarkContainer = Color(0xFF7D2814)
val OnTerracottaDarkContainer = Color(0xFFFFDBD2)

val SageDarkSecondary = Color(0xFF74C69D)
val OnSageDarkSecondary = Color(0xFF0A3824)
val SageDarkContainer = Color(0xFF1B4D36)
val OnSageDarkContainer = Color(0xFFD1EFE0)

val AmberDarkTertiary = Color(0xFFFBBF24)
val OnAmberDarkTertiary = Color(0xFF472100)
val AmberDarkContainer = Color(0xFF693400)
val OnAmberDarkContainer = Color(0xFFFDE68A)

val EspressoBackground = Color(0xFF161311)
val OnEspressoBackground = Color(0xFFEEE6DF)
val WalnutSurface = Color(0xFF211C19)
val OnWalnutSurface = Color(0xFFEEE6DF)
val WalnutSurfaceVariant = Color(0xFF332B26)
val OnWalnutSurfaceVariant = Color(0xFFD6C8BE)
val EspressoOutline = Color(0xFF9E9086)
val EspressoOutlineVariant = Color(0xFF4E433C)

@Immutable
data class ExtendedReminderColors(
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val priorityHigh: Color,
    val priorityHighContainer: Color,
    val priorityMedium: Color,
    val priorityMediumContainer: Color,
    val priorityLow: Color,
    val priorityLowContainer: Color,
    val categoryPersonal: Color,
    val categoryWork: Color,
    val categoryHealth: Color,
    val categoryFinance: Color,
    val categoryStudy: Color,
    val categoryShopping: Color,
    val successAccent: Color,
    val successContainer: Color
)

val LightExtendedColors = ExtendedReminderColors(
    heroGradientStart = Color(0xFFFFEFE8),
    heroGradientEnd = Color(0xFFFBF8F3),
    priorityHigh = Color(0xFFC8322B),
    priorityHighContainer = Color(0xFFFFE2DF),
    priorityMedium = Color(0xFFB45309),
    priorityMediumContainer = Color(0xFFFEF3C7),
    priorityLow = Color(0xFF2D6A4F),
    priorityLowContainer = Color(0xFFD8F3DC),
    categoryPersonal = Color(0xFFC84B31),
    categoryWork = Color(0xFF1D4ED8),
    categoryHealth = Color(0xFF15803D),
    categoryFinance = Color(0xFFB45309),
    categoryStudy = Color(0xFF6D28D9),
    categoryShopping = Color(0xFF0F766E),
    successAccent = Color(0xFF1B7A4E),
    successContainer = Color(0xFFD6F5E5)
)

val DarkExtendedColors = ExtendedReminderColors(
    heroGradientStart = Color(0xFF2E1E19),
    heroGradientEnd = Color(0xFF161311),
    priorityHigh = Color(0xFFFF897D),
    priorityHighContainer = Color(0xFF5C1914),
    priorityMedium = Color(0xFFFBBF24),
    priorityMediumContainer = Color(0xFF523206),
    priorityLow = Color(0xFF74C69D),
    priorityLowContainer = Color(0xFF163D2A),
    categoryPersonal = Color(0xFFFF8A65),
    categoryWork = Color(0xFF93C5FD),
    categoryHealth = Color(0xFF86EFAC),
    categoryFinance = Color(0xFFFCD34D),
    categoryStudy = Color(0xFFC4B5FD),
    categoryShopping = Color(0xFF5EEAD4),
    successAccent = Color(0xFF6EE7B7),
    successContainer = Color(0xFF134530)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
