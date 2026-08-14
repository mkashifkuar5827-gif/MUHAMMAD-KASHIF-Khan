package com.example.ui.locale

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Supported application languages with RTL metadata and display labels.
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean,
    val flagEmoji: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        isRtl = false,
        flagEmoji = "EN"
    ),
    URDU(
        code = "ur",
        displayName = "Urdu",
        nativeName = "اردو",
        isRtl = true,
        flagEmoji = "اردو"
    ),
    ARABIC(
        code = "ar",
        displayName = "Arabic",
        nativeName = "العربية",
        isRtl = true,
        flagEmoji = "عربي"
    );

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    fun next(): AppLanguage {
        return when (this) {
            ENGLISH -> URDU
            URDU -> ARABIC
            ARABIC -> ENGLISH
        }
    }

    companion object {
        fun fromCode(code: String?): AppLanguage {
            if (code.isNullOrBlank()) return ENGLISH
            return entries.find { it.code.equals(code.trim(), ignoreCase = true) } ?: ENGLISH
        }
    }
}

/**
 * Central state manager responsible for managing application locale,
 * cycling/toggling between languages, and enforcing automatic RTL/LTR direction.
 */
object LanguageStateManager {
    private val _currentLanguage = mutableStateOf(AppLanguage.ENGLISH)
    val currentLanguageState: State<AppLanguage> = _currentLanguage

    private val _languageFlow = MutableStateFlow(AppLanguage.ENGLISH)
    val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

    val currentLanguage: AppLanguage
        get() = _currentLanguage.value

    val layoutDirection: LayoutDirection
        get() = _currentLanguage.value.layoutDirection

    val isRtl: Boolean
        get() = _currentLanguage.value.isRtl

    /**
     * Initializes state from stored preferences / profile code.
     */
    fun initialize(languageCode: String?) {
        val lang = AppLanguage.fromCode(languageCode)
        _currentLanguage.value = lang
        _languageFlow.value = lang
    }

    /**
     * Sets specific language and updates global state.
     */
    fun setLanguage(language: AppLanguage, context: Context? = null) {
        _currentLanguage.value = language
        _languageFlow.value = language
        context?.let { applyLocaleToContext(it, language) }
    }

    /**
     * Sets language from code string (e.g. "en", "ur", "ar").
     */
    fun setLanguageByCode(code: String, context: Context? = null): AppLanguage {
        val lang = AppLanguage.fromCode(code)
        setLanguage(lang, context)
        return lang
    }

    /**
     * Toggles to the next language in sequence:
     * English -> Urdu (RTL) -> Arabic (RTL) -> English (LTR)
     */
    fun toggleNextLanguage(context: Context? = null): AppLanguage {
        val next = _currentLanguage.value.next()
        setLanguage(next, context)
        return next
    }

    /**
     * Updates Java / Android default locale for standard formatting operations.
     */
    fun applyLocaleToContext(context: Context, language: AppLanguage) {
        try {
            val locale = when (language) {
                AppLanguage.ENGLISH -> Locale.ENGLISH
                AppLanguage.URDU -> Locale("ur", "PK")
                AppLanguage.ARABIC -> Locale("ar", "SA")
            }
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
        } catch (_: Exception) {
            // Safe fallback
        }
    }
}

/**
 * Composable wrapper providing both LocalAppLanguage and LocalLayoutDirection
 * to the Compose hierarchy. Ensures immediate UI rebuild with RTL when switching to Urdu or Arabic.
 */
@Composable
fun ProvideAppLanguageState(
    language: AppLanguage = LanguageStateManager.currentLanguageState.value,
    content: @Composable () -> Unit
) {
    val layoutDir = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDir
    ) {
        content()
    }
}
