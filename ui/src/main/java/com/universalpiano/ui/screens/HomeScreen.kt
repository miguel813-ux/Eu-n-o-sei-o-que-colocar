package com.universalpiano.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.universalpiano.ui.Routes
import com.universalpiano.ui.components.FeatureCard

private data class HomeItem(
    val route: String,
    val glyph: String,
    val title: String,
    val subtitle: String,
)

private val homeItems = listOf(
    HomeItem(Routes.PLAY, "🎹", "Tocar", "Teclado com multitouch"),
    HomeItem(Routes.INSTRUMENTS, "🎼", "Instrumentos", "7 timbres para escolher"),
    HomeItem(Routes.LIBRARY, "📚", "Biblioteca", "Gravações e músicas salvas"),
    HomeItem(Routes.LAYOUTS, "🧩", "Layouts", "Perfis e disposição do teclado"),
    HomeItem(Routes.COMPATIBILITY, "🎮", "Compatibilidade", "Métodos para usar com jogos"),
    HomeItem(Routes.SETTINGS, "⚙️", "Configurações", "Tema, sensibilidade e mais"),
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Universal Piano",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = "Escolha por onde começar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(homeItems, key = { it.route }) { item ->
                    FeatureCard(
                        glyph = item.glyph,
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = { onNavigate(item.route) },
                    )
                }
            }
        }
    }
}
