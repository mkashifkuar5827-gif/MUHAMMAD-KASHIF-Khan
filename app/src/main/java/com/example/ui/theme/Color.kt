package com.example.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewColor: Color,
    val previewBg: Color,
    val isDark: Boolean = true
) {
    GOLD(
        id = "GOLD",
        title = "Royal Gold & Onyx",
        subtitle = "Flagship luxury black with radiant gold accents",
        previewColor = Color(0xFFD4AF37),
        previewBg = Color(0xFF101012),
        isDark = true
    ),
    CYBER_CYAN(
        id = "CYBER_CYAN",
        title = "Cyber Neon Cyan",
        subtitle = "Modern high-tech midnight with electric cyan",
        previewColor = Color(0xFF00E5FF),
        previewBg = Color(0xFF0B1120),
        isDark = true
    ),
    EMERALD(
        id = "EMERALD",
        title = "Emerald Business",
        subtitle = "Premium dark slate with vibrant emerald green",
        previewColor = Color(0xFF10B981),
        previewBg = Color(0xFF061A14),
        isDark = true
    ),
    VIOLET(
        id = "VIOLET",
        title = "Royal Nebula Violet",
        subtitle = "Deep amethyst with luminous electric purple",
        previewColor = Color(0xFFA855F7),
        previewBg = Color(0xFF120B1E),
        isDark = true
    ),
    CRIMSON(
        id = "CRIMSON",
        title = "Titanium Crimson",
        subtitle = "High-performance dark carbon with vivid red",
        previewColor = Color(0xFFEF4444),
        previewBg = Color(0xFF120B0B),
        isDark = true
    ),
    CLEAN_LIGHT(
        id = "CLEAN_LIGHT",
        title = "Crystal Light Mode",
        subtitle = "Crisp daytime white canvas with royal blue accents",
        previewColor = Color(0xFF2563EB),
        previewBg = Color(0xFFF8FAFC),
        isDark = false
    );

    companion object {
        fun fromId(id: String?): AppThemeMode =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: GOLD
    }
}

data class ThemePalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val container: Color,
    val onContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

fun getThemePalette(mode: AppThemeMode): ThemePalette {
    return when (mode) {
        AppThemeMode.GOLD -> ThemePalette(
            primary = Color(0xFFD4AF37),
            secondary = Color(0xFFFFD700),
            tertiary = Color(0xFFE5C158),
            container = Color(0xFF2E2612),
            onContainer = Color(0xFFFFE58F),
            background = Color(0xFF101012),
            surface = Color(0xFF18181B),
            surfaceVariant = Color(0xFF242428),
            border = Color(0xFF333338),
            textPrimary = Color(0xFFF4F4F5),
            textSecondary = Color(0xFFA1A1AA),
            textMuted = Color(0xFF71717A),
            isDark = true
        )
        AppThemeMode.CYBER_CYAN -> ThemePalette(
            primary = Color(0xFF00E5FF),
            secondary = Color(0xFF38BDF8),
            tertiary = Color(0xFF06B6D4),
            container = Color(0xFF082F49),
            onContainer = Color(0xFFBAE6FD),
            background = Color(0xFF0B1120),
            surface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFF1E293B),
            border = Color(0xFF334155),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B),
            isDark = true
        )
        AppThemeMode.EMERALD -> ThemePalette(
            primary = Color(0xFF10B981),
            secondary = Color(0xFF34D399),
            tertiary = Color(0xFF059669),
            container = Color(0xFF064E3B),
            onContainer = Color(0xFFA7F3D0),
            background = Color(0xFF061A14),
            surface = Color(0xFF0B2920),
            surfaceVariant = Color(0xFF11382C),
            border = Color(0xFF1D4D3E),
            textPrimary = Color(0xFFF0FDF4),
            textSecondary = Color(0xFF86EFAC),
            textMuted = Color(0xFF6EE7B7),
            isDark = true
        )
        AppThemeMode.VIOLET -> ThemePalette(
            primary = Color(0xFFA855F7),
            secondary = Color(0xFFC084FC),
            tertiary = Color(0xFF9333EA),
            container = Color(0xFF3B0764),
            onContainer = Color(0xFFE9D5FF),
            background = Color(0xFF120B1E),
            surface = Color(0xFF1E1035),
            surfaceVariant = Color(0xFF2E1A4E),
            border = Color(0xFF4C2A7A),
            textPrimary = Color(0xFFFAF5FF),
            textSecondary = Color(0xFFD8B4FE),
            textMuted = Color(0xFFA855F7),
            isDark = true
        )
        AppThemeMode.CRIMSON -> ThemePalette(
            primary = Color(0xFFEF4444),
            secondary = Color(0xFFF87171),
            tertiary = Color(0xFFDC2626),
            container = Color(0xFF450A0A),
            onContainer = Color(0xFFFECACA),
            background = Color(0xFF120B0B),
            surface = Color(0xFF1C1111),
            surfaceVariant = Color(0xFF291818),
            border = Color(0xFF442424),
            textPrimary = Color(0xFFFEF2F2),
            textSecondary = Color(0xFFFCA5A5),
            textMuted = Color(0xFF991B1B),
            isDark = true
        )
        AppThemeMode.CLEAN_LIGHT -> ThemePalette(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF3B82F6),
            tertiary = Color(0xFF1D4ED8),
            container = Color(0xFFDBEAFE),
            onContainer = Color(0xFF1E40AF),
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF1F5F9),
            border = Color(0xFFE2E8F0),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textMuted = Color(0xFF94A3B8),
            isDark = false
        )
    }
}

val currentPaletteState = mutableStateOf(getThemePalette(AppThemeMode.GOLD))

val GoldPrimary: Color get() = currentPaletteState.value.primary
val GoldSecondary: Color get() = currentPaletteState.value.secondary
val GoldTertiary: Color get() = currentPaletteState.value.tertiary
val GoldContainer: Color get() = currentPaletteState.value.container
val OnGoldContainer: Color get() = currentPaletteState.value.onContainer

val BlackBackground: Color get() = currentPaletteState.value.background
val DarkSurface: Color get() = currentPaletteState.value.surface
val DarkSurfaceVariant: Color get() = currentPaletteState.value.surfaceVariant
val DarkBorder: Color get() = currentPaletteState.value.border

val TextPrimary: Color get() = currentPaletteState.value.textPrimary
val TextSecondary: Color get() = currentPaletteState.value.textSecondary
val TextMuted: Color get() = currentPaletteState.value.textMuted

val StatusPending = Color(0xFFF59E0B) // Amber
val StatusRepairing = Color(0xFF38BDF8) // Sky Blue
val StatusCompleted = Color(0xFF10B981) // Emerald Green
val StatusDelivered = Color(0xFF06B6D4) // Cyan / Teal
val StatusCancelled = Color(0xFFEF4444) // Red

val SuccessGreen = Color(0xFF22C55E)
val DangerRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)
