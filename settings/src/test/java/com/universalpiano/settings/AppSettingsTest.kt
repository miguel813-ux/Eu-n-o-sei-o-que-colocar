package com.universalpiano.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {

    @Test
    fun `valor valido e convertido`() {
        assertEquals(ThemeMode.DARK, parseEnumOrDefault("DARK", ThemeMode.SYSTEM))
        assertEquals(AccentPalette.TEAL, parseEnumOrDefault("TEAL", AccentPalette.VIOLET))
    }

    @Test
    fun `valor nulo ou desconhecido volta ao padrao`() {
        assertEquals(ThemeMode.SYSTEM, parseEnumOrDefault(null, ThemeMode.SYSTEM))
        assertEquals(ThemeMode.SYSTEM, parseEnumOrDefault("NAO_EXISTE", ThemeMode.SYSTEM))
    }

    @Test
    fun `padroes do AppSettings`() {
        val s = AppSettings()
        assertEquals(ThemeMode.SYSTEM, s.themeMode)
        assertEquals(true, s.glissando)
        assertEquals(0.5f, s.keySensitivity, 0f)
    }
}
