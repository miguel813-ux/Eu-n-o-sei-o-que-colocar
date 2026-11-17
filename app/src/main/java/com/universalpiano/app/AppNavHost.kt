package com.universalpiano.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.universalpiano.settings.AppSettings
import com.universalpiano.settings.SettingsRepository
import com.universalpiano.ui.Routes
import com.universalpiano.ui.screens.HomeScreen
import com.universalpiano.ui.screens.PlaceholderScreen
import com.universalpiano.ui.screens.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    settings: AppSettings,
    repository: SettingsRepository,
    scope: CoroutineScope,
) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onNavigate = { route -> nav.navigate(route) { launchSingleTop = true } })
        }
        composable(Routes.PLAY) {
            PlaceholderScreen("Tocar", "ETAPA 3 (teclado) e ETAPA 4 (áudio)", back)
        }
        composable(Routes.INSTRUMENTS) {
            PlaceholderScreen("Instrumentos", "ETAPA 4 (áudio e instrumentos)", back)
        }
        composable(Routes.LIBRARY) {
            PlaceholderScreen("Biblioteca", "ETAPA 7 (gravador, MIDI e biblioteca)", back)
        }
        composable(Routes.LAYOUTS) {
            PlaceholderScreen("Layouts", "ETAPA 5 (personalização)", back)
        }
        composable(Routes.COMPATIBILITY) {
            PlaceholderScreen("Compatibilidade", "ETAPA 6 (integração)", back)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onThemeMode = { scope.launch { repository.setThemeMode(it) } },
                onAccent = { scope.launch { repository.setAccent(it) } },
                onGlissando = { scope.launch { repository.setGlissando(it) } },
                onSensitivity = { scope.launch { repository.setKeySensitivity(it) } },
                onBack = back,
            )
        }
    }
}
