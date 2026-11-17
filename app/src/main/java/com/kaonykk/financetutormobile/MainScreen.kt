package com.kaonykk.financetutormobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.kaonykk.financetutormobile.data.model.LoginResponse

@Composable
fun MainScreen(user: LoginResponse, onLogout: () -> Unit) {
    val tabs = when (user.role) {
        "PRINCIPAL" -> listOf("Inicio" to "🏠", "Solicitudes" to "🔔", "Reportes" to "📊", "Perfil" to "👤")
        "TUTOR" -> listOf("Aprobaciones" to "✅", "Perfil" to "👤")
        else -> listOf("Perfil" to "👤")
    }
    var selected by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Text(icon, fontSize = 20.sp) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = Midnight,
                            indicatorColor = CopperLight
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tabs[selected].first) {
                "Inicio" -> HomeScreen(user)
                "Solicitudes" -> RequestsScreen()
                "Reportes" -> ReportsScreen()
                "Aprobaciones" -> ApprovalsScreen()
                else -> ProfileScreen(user, onLogout)
            }
        }
    }
}