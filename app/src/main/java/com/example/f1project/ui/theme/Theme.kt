package com.example.f1project.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.f1project.R

// ─────────────────────────────────────────────
// KOLORY ZESPOŁÓW
// ─────────────────────────────────────────────

val teamColors = mapOf(
    "mercedes"     to Color(0xFF00D2BE),
    "ferrari"      to Color(0xFFDC0000),
    "red_bull"     to Color(0xFF0600EF),
    "mclaren"      to Color(0xFFFF8700),
    "alpine"       to Color(0xFF0090FF),
    "rb"           to Color(0xFF0032FF),
    "sauber"       to Color(0xFF00FF00),
    "aston_martin" to Color(0xFF006F62),
    "williams"     to Color(0xFF005AFF),
    "haas"         to Color(0xFFB6BABD)
)

// ─────────────────────────────────────────────
// KOLORY PODIUM
// ─────────────────────────────────────────────

object PodiumColors {
    val gold   = Color(0xFFFFD700)
    val silver = Color(0xFFC0C0C0)
    val bronze = Color(0xFFCD7F32)

    fun forPosition(position: Int): Color? = when (position) {
        1 -> gold
        2 -> silver
        3 -> bronze
        else -> null
    }
}

// ─────────────────────────────────────────────
// KOLORY MOTYWU — DARK
// ─────────────────────────────────────────────

object F1DarkColors {
    val primary            = Color(0xFFFF0000) // czerwień F1
    val onPrimary          = Color(0xFFFFFFFF)
    val primaryContainer   = Color(0xFF5C0000) // ciemna czerwień — tło switcha
    val onPrimaryContainer = Color(0xFFFFDAD6)
    val secondary          = Color(0xFFB0B0B0) // jasnoszary akcent
    val onSecondary        = Color(0xFF1A1A1A)
    val secondaryContainer = Color(0xFF2E2E2E)
    val onSecondaryContainer = Color(0xFFE0E0E0)
    val background         = Color(0xFF111111) // prawie czarny
    val onBackground       = Color(0xFFFFFFFF)
    val surface            = Color(0xFF1E1E1E) // karta
    val onSurface          = Color(0xFFFFFFFF)
    val surfaceVariant     = Color(0xFF2A2A2A)
    val onSurfaceVariant   = Color(0xFFAAAAAA)
    val outline            = Color(0xFF555555)
    val outlineVariant     = Color(0xFF333333)
    val error              = Color(0xFFFF6B6B)
    val onError            = Color(0xFF1A1A1A)
    val tertiaryContainer  = Color(0xFF2D2000) // cache banner tło
    val onTertiaryContainer = Color(0xFFFFCC66)
}

// ─────────────────────────────────────────────
// KOLORY MOTYWU — LIGHT
// ─────────────────────────────────────────────

object F1LightColors {
    val primary            = Color(0xFFCC0000) // nieco ciemniejsza czerwień — czytelna na białym
    val onPrimary          = Color(0xFFFFFFFF)
    val primaryContainer   = Color(0xFFFFDAD6)
    val onPrimaryContainer = Color(0xFF5C0000)
    val secondary          = Color(0xFF555555) // ciemnoszary akcent
    val onSecondary        = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFFE8E8E8)
    val onSecondaryContainer = Color(0xFF1A1A1A)
    val background         = Color(0xFFF4F4F4) // jasnoszare tło
    val onBackground       = Color(0xFF111111)
    val surface            = Color(0xFFFFFFFF) // białe karty
    val onSurface          = Color(0xFF111111)
    val surfaceVariant     = Color(0xFFEEEEEE)
    val onSurfaceVariant   = Color(0xFF555555)
    val outline            = Color(0xFFBBBBBB)
    val outlineVariant     = Color(0xFFDDDDDD)
    val error              = Color(0xFFB00020)
    val onError            = Color(0xFFFFFFFF)
    val tertiaryContainer  = Color(0xFFFFF3CD)
    val onTertiaryContainer = Color(0xFF664D00)
}

private val DarkColorScheme = darkColorScheme(
    primary                = F1DarkColors.primary,
    onPrimary              = F1DarkColors.onPrimary,
    primaryContainer       = F1DarkColors.primaryContainer,
    onPrimaryContainer     = F1DarkColors.onPrimaryContainer,
    secondary              = F1DarkColors.secondary,
    onSecondary            = F1DarkColors.onSecondary,
    secondaryContainer     = F1DarkColors.secondaryContainer,
    onSecondaryContainer   = F1DarkColors.onSecondaryContainer,
    background             = F1DarkColors.background,
    onBackground           = F1DarkColors.onBackground,
    surface                = F1DarkColors.surface,
    onSurface              = F1DarkColors.onSurface,
    surfaceVariant         = F1DarkColors.surfaceVariant,
    onSurfaceVariant       = F1DarkColors.onSurfaceVariant,
    outline                = F1DarkColors.outline,
    outlineVariant         = F1DarkColors.outlineVariant,
    error                  = F1DarkColors.error,
    onError                = F1DarkColors.onError,
    tertiaryContainer      = F1DarkColors.tertiaryContainer,
    onTertiaryContainer    = F1DarkColors.onTertiaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary                = F1LightColors.primary,
    onPrimary              = F1LightColors.onPrimary,
    primaryContainer       = F1LightColors.primaryContainer,
    onPrimaryContainer     = F1LightColors.onPrimaryContainer,
    secondary              = F1LightColors.secondary,
    onSecondary            = F1LightColors.onSecondary,
    secondaryContainer     = F1LightColors.secondaryContainer,
    onSecondaryContainer   = F1LightColors.onSecondaryContainer,
    background             = F1LightColors.background,
    onBackground           = F1LightColors.onBackground,
    surface                = F1LightColors.surface,
    onSurface              = F1LightColors.onSurface,
    surfaceVariant         = F1LightColors.surfaceVariant,
    onSurfaceVariant       = F1LightColors.onSurfaceVariant,
    outline                = F1LightColors.outline,
    outlineVariant         = F1LightColors.outlineVariant,
    error                  = F1LightColors.error,
    onError                = F1LightColors.onError,
    tertiaryContainer      = F1LightColors.tertiaryContainer,
    onTertiaryContainer    = F1LightColors.onTertiaryContainer
)

// ─────────────────────────────────────────────
// TYPOGRAFIA
// ─────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val robotoCondensed = GoogleFont("Roboto Condensed")

val F1FontFamily = FontFamily(
    Font(googleFont = robotoCondensed, fontProvider = provider),
    Font(
        googleFont = robotoCondensed,
        fontProvider = provider,
        weight = FontWeight.Bold
    )
)

val F1Typography = Typography(
    displayLarge = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 52.sp,
        lineHeight   = 56.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 32.sp,
        lineHeight   = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 26.sp,
        lineHeight   = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 20.sp,
        lineHeight   = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 16.sp,
        lineHeight   = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 15.sp,
        lineHeight   = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 13.sp,
        lineHeight   = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily   = F1FontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily    = F1FontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 13.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = F1FontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 11.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = F1FontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        letterSpacing = 1.sp
    )
)

// Alias dla kompatybilności wstecznej z istniejącym kodem
val Typography = F1Typography

// ─────────────────────────────────────────────
// WYMIARY — wszystkie dp w jednym miejscu
// ─────────────────────────────────────────────

object F1Dimens {
    // Odstępy
    val spacingXs: Dp   = 4.dp
    val spacingS: Dp    = 8.dp
    val spacingM: Dp    = 14.dp
    val spacingL: Dp    = 16.dp
    val spacingXl: Dp   = 24.dp
    val spacingXxl: Dp  = 32.dp

    // Karty
    val cardPaddingH: Dp      = 16.dp
    val cardPaddingV: Dp      = 14.dp
    val cardElevation: Dp     = 2.dp
    val cardElevationHigh: Dp = 4.dp
    val cardCornerRadius: Dp  = 12.dp
    val cardCornerRadiusS: Dp = 8.dp

    // Elementy
    val teamBarWidth: Dp      = 5.dp
    val positionBadgeSize: Dp = 38.dp
    val positionColumnWidth: Dp = 44.dp
    val iconSize: Dp          = 22.dp
    val dividerThickness: Dp  = 1.dp

    // Listy
    val listPaddingH: Dp      = 16.dp
    val listPaddingV: Dp      = 20.dp
    val listItemSpacing: Dp   = 8.dp

    // TopBar
    val topBarElevation: Dp   = 0.dp

    // Widget (używane w F1Widget.kt)
    val widgetPadding: Dp     = 12.dp
    val widgetBarWidth: Dp    = 3.dp
    val widgetBarHeight: Dp   = 14.dp

    // Paddingów offset dla linii czasów w wynikach
    val timesRowStartPadding: Dp = 63.dp
}

// ─────────────────────────────────────────────
// FLAGI NARODOWOŚCI
// ─────────────────────────────────────────────

val nationalityToCode = mapOf(
    "American"      to "US", "Argentine"     to "AR", "Australian"    to "AU",
    "Austrian"      to "AT", "Belgian"       to "BE", "Brazilian"     to "BR",
    "British"       to "GB", "Canadian"      to "CA", "Chinese"       to "CN",
    "Colombian"     to "CO", "Czech"         to "CZ", "Danish"        to "DK",
    "Dutch"         to "NL", "Finnish"       to "FI", "French"        to "FR",
    "German"        to "DE", "Hungarian"     to "HU", "Indian"        to "IN",
    "Indonesian"    to "ID", "Irish"         to "IE", "Italian"       to "IT",
    "Japanese"      to "JP", "Malaysian"     to "MY", "Mexican"       to "MX",
    "Monegasque"    to "MC", "New Zealander" to "NZ", "Polish"        to "PL",
    "Portuguese"    to "PT", "Russian"       to "RU", "South African" to "ZA",
    "Spanish"       to "ES", "Swedish"       to "SE", "Swiss"         to "CH",
    "Thai"          to "TH", "Uruguayan"     to "UY", "Venezuelan"    to "VE"
)

val alpha3ToAlpha2 = mapOf(
    "AUT" to "AT", "ARG" to "AR", "AUS" to "AU", "BEL" to "BE",
    "BRA" to "BR", "CAN" to "CA", "CHN" to "CN", "COL" to "CO",
    "CZE" to "CZ", "DEN" to "DK", "FIN" to "FI", "FRA" to "FR",
    "GBR" to "GB", "GER" to "DE", "HUN" to "HU", "IND" to "IN",
    "INA" to "ID", "IRL" to "IE", "ITA" to "IT", "JPN" to "JP",
    "MAL" to "MY", "MEX" to "MX", "MON" to "MC", "NED" to "NL",
    "NZL" to "NZ", "POL" to "PL", "POR" to "PT", "RSA" to "ZA",
    "RUS" to "RU", "ESP" to "ES", "SWE" to "SE", "SUI" to "CH",
    "THA" to "TH", "URU" to "UY", "USA" to "US", "VEN" to "VE"
)

val driverAcronymToCountry = mapOf(
    "VER" to "NL", "NOR" to "GB", "LEC" to "MC", "PIA" to "AU",
    "SAI" to "ES", "RUS" to "GB", "HAM" to "GB", "ANT" to "GB",
    "ALO" to "ES", "STR" to "CA", "GAS" to "FR", "OCO" to "FR",
    "ALB" to "TH", "COL" to "FR", "TSU" to "JP", "HAD" to "NZ",
    "HUL" to "DE", "BEA" to "AU", "BOR" to "BR", "DOO" to "AU",
    "LAW" to "NZ", "MAG" to "DK", "ZHO" to "CN", "BOT" to "FI",
    "PER" to "MX", "DEV" to "NL", "SAR" to "US", "MSC" to "DE",
    "VET" to "DE", "RAI" to "FI", "KUB" to "PL"
)

fun countryCodeToFlag(code: String): String {
    if (code.length != 2) return ""
    return code.uppercase().map { char ->
        String(Character.toChars(0x1F1E6 + (char - 'A')))
    }.joinToString("")
}

fun getFlag(nationality: String): String {
    if (nationality.isBlank()) return ""
    if (nationality.length == 2) return countryCodeToFlag(nationality)
    if (nationality.length == 3) {
        val driverCountry = driverAcronymToCountry[nationality.uppercase()]
        if (driverCountry != null) return countryCodeToFlag(driverCountry)
        val alpha2 = alpha3ToAlpha2[nationality.uppercase()]
        if (alpha2 != null) return countryCodeToFlag(alpha2)
    }
    val alpha2 = nationalityToCode[nationality]
    if (alpha2 != null) return countryCodeToFlag(alpha2)
    return ""
}

val nationalityFlags: Map<String, String> by lazy {
    nationalityToCode.mapValues { (_, code) -> countryCodeToFlag(code) }
}

// ─────────────────────────────────────────────
// MOTYW GŁÓWNY
// ─────────────────────────────────────────────

@Composable
fun F1ProjectTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = F1Typography,
        content     = content
    )
}