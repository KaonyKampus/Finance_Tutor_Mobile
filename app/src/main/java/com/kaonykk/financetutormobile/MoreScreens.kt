package com.kaonykk.financetutormobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaonykk.financetutormobile.data.model.*
import com.kaonykk.financetutormobile.data.remote.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RequestsScreen() {
    var list by remember { mutableStateOf<List<RequestResponse>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val r = ApiClient.api.requests()
            if (r.isSuccessful) list = r.body() ?: emptyList()
            else error = parseError(r.errorBody()?.string())
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    val pending = list?.count { it.status == "PENDING" } ?: 0

    LazyColumn(
        Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Mis solicitudes", "Retiros que superaron tu límite diario") }
        error?.let { msg -> item { Notice(msg, Red, RedBg) } }
        if (list == null && error == null) item { Loading() }
        if (pending > 0) item { Notice("Tienes $pending solicitud(es) esperando a tu tutor", Amber, AmberBg) }
        if (list?.isEmpty() == true) item { Text("No tienes solicitudes.", color = Muted) }
        items(list ?: emptyList()) { r -> RequestCard(r) }
    }
}

@Composable
fun ApprovalsScreen() {
    var list by remember { mutableStateOf<List<RequestResponse>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        try {
            val r = ApiClient.api.requests()
            if (r.isSuccessful) list = r.body() ?: emptyList()
            else error = parseError(r.errorBody()?.string())
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    fun decide(id: Long, action: String) {
        busy = true
        error = null
        info = null
        scope.launch {
            try {
                val r = ApiClient.api.decide(DecisionRequest(id, action))
                if (r.isSuccessful) info = r.body()?.message
                else error = parseError(r.errorBody()?.string())
                load()
            } catch (e: Exception) {
                error = "No hay conexión con el servidor"
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    LazyColumn(
        Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Aprobaciones", "Retiros de tus tutelados que superaron el límite") }
        error?.let { msg -> item { Notice(msg, Red, RedBg) } }
        info?.let { msg -> item { Notice(msg, Green, GreenBg) } }
        if (list == null && error == null) item { Loading() }
        if (list?.isEmpty() == true) item { Text("No hay solicitudes pendientes.", color = Muted) }
        items(list ?: emptyList()) { r ->
            RequestCard(r) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { decide(r.id, "approve") }, enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Aprobar", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { decide(r.id, "reject") }, enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Rechazar", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen() {
    var history by remember { mutableStateOf<List<TransactionResponse>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val r = ApiClient.api.transactions()
            if (r.isSuccessful) history = r.body() ?: emptyList()
            else error = parseError(r.errorBody()?.string())
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    val data = history ?: emptyList()
    val ingresos = data.filter { it.type == "INCOME" }.sumOf { it.amount }
    val gastos = data.filter { it.type == "WITHDRAWAL" }.sumOf { it.amount }
    val maxValue = maxOf(ingresos, gastos, 1.0)
    val mayorGasto = data.filter { it.type == "WITHDRAWAL" }.maxByOrNull { it.amount }

    LazyColumn(
        Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("Reporte de gastos", "Resumen de todos tus movimientos") }
        error?.let { msg -> item { Notice(msg, Red, RedBg) } }
        if (history == null && error == null) item { Loading() }
        if (history != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Ingresos", money(ingresos), Green, Modifier.weight(1f))
                    StatCard("Gastos", money(gastos), Red, Modifier.weight(1f))
                }
            }
            item { StatCard("Balance", money(ingresos - gastos), Midnight, Modifier.fillMaxWidth()) }
            item {
                WhiteCard {
                    Column(Modifier.padding(18.dp)) {
                        Text("Ingresos vs. gastos", fontWeight = FontWeight.SemiBold, color = Midnight, fontSize = 17.sp)
                        Spacer(Modifier.height(12.dp))
                        Bar("Ingresos", ingresos, maxValue, Green)
                        Spacer(Modifier.height(12.dp))
                        Bar("Gastos", gastos, maxValue, Red)
                    }
                }
            }
            item {
                WhiteCard {
                    Column(Modifier.padding(18.dp)) {
                        Text("Resumen", fontWeight = FontWeight.SemiBold, color = Midnight, fontSize = 17.sp)
                        Spacer(Modifier.height(8.dp))
                        InfoRow("Movimientos registrados", "${data.size}")
                        InfoRow("Ingresos", "${data.count { it.type == "INCOME" }}")
                        InfoRow("Retiros", "${data.count { it.type == "WITHDRAWAL" }}")
                        mayorGasto?.let { InfoRow("Mayor gasto", money(it.amount)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(user: LoginResponse, onLogout: () -> Unit) {
    var account by remember { mutableStateOf<AccountResponse?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (user.role == "PRINCIPAL") {
            try {
                val r = ApiClient.api.account()
                if (r.isSuccessful) account = r.body()
            } catch (e: Exception) {
            }
        }
    }

    val roleLabel = when (user.role) {
        "PRINCIPAL" -> "Usuario principal"
        "TUTOR" -> "Tutor"
        "ADMIN" -> "Administrador"
        else -> user.role
    }

    Column(
        Modifier.fillMaxSize().background(Bg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle("Mi perfil")
        WhiteCard {
            Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(Midnight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(1).uppercase(), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Midnight)
                Spacer(Modifier.height(6.dp))
                Pill(roleLabel, Copper, Color(0xFFF6EADF))
            }
        }
        account?.let { acc ->
            WhiteCard {
                Column(Modifier.padding(18.dp)) {
                    InfoRow("Número de cuenta", "#${acc.id}")
                    InfoRow("Saldo", money(acc.balance))
                    InfoRow("Límite diario", money(acc.dailyLimit))
                }
            }
        }
        if (user.role == "ADMIN") {
            Notice("La gestión de usuarios se realiza desde la aplicación web.", Amber, AmberBg)
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        ApiClient.api.logout()
                    } catch (e: Exception) {
                    }
                    onLogout()
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Cerrar sesión", color = Red, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun RequestCard(r: RequestResponse, actions: (@Composable () -> Unit)? = null) {
    WhiteCard {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(r.requestedBy, fontWeight = FontWeight.Medium, color = TextDark)
                    Text(shortDate(r.createdAt), fontSize = 12.sp, color = Muted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(money(r.amount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Midnight)
                    Spacer(Modifier.height(4.dp))
                    StatusPill(r.status)
                }
            }
            if (actions != null) {
                Spacer(Modifier.height(12.dp))
                actions()
            }
        }
    }
}

@Composable
private fun WhiteCard(modifier: Modifier = Modifier.fillMaxWidth(), content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier,
        content = content
    )
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    WhiteCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label.uppercase(), fontSize = 12.sp, color = Muted, letterSpacing = 1.sp)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun Bar(label: String, value: Double, max: Double, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextDark)
            Text(money(value), color = color, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(50)).background(Bg)) {
            Box(
                Modifier.fillMaxWidth((value / max).toFloat()).fillMaxHeight()
                    .clip(RoundedCornerShape(50)).background(color)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted)
        Text(value, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}