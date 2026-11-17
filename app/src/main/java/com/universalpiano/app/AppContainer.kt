package com.universalpiano.app

import android.content.Context
import com.universalpiano.settings.SettingsRepository

/**
 * DI manual (decisão do ARQUITETURA.md, seção 9): um contêiner simples,
 * criado uma vez na Application. Novos módulos (áudio, gravador...) entram aqui.
 */
class AppContainer(context: Context) {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }
}
