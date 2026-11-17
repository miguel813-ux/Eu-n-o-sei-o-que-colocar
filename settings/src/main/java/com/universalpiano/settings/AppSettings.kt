package com.universalpiano.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AccentPalette { VIOLET, TEAL, AMBER }

/**
 * Preferências do app. Valores padrão são usados quando nada foi salvo.
 * [keySensitivity] vai de 0f a 1f (usado pelo teclado na ETAPA 3).
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: AccentPalette = AccentPalette.VIOLET,
    val glissando: Boolean = true,
    val keySensitivity: Float = 0.5f,
)

/** Converte texto salvo em enum, com fallback seguro para valores desconhecidos/corrompidos. */
inline fun <reified T : Enum<T>> parseEnumOrDefault(name: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default
