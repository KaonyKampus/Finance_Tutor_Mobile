package com.kaonykk.financetutormobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaonykk.financetutormobile.data.model.*
import com.kaonykk.financetutormobile.data.remote.ApiClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLogin: (LoginResponse) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(Midnight, MidnightSoft, Color(0xFF3A2A20))))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                Row {
                    Text("Finance", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Midnight)
                    Text("Tutor", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Copper)
                }
                Text("Gestor de finanzas tutelado", color = Muted)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Correo") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Contraseña") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Notice(it, Red, RedBg)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            error = "Completa todos los campos"
                            return@Button
                        }
                        loading = true
                        error = null
                        scope.launch {
                            try {
                                val resp = ApiClient.api.login(LoginRequest(email.trim(), password))
                                val body = resp.body()
                                if (resp.isSuccessful && body != null) onLogin(body)
                                else error = if (resp.code() == 401) "Correo o contraseña incorrectos"
                                else parseError(resp.errorBody()?.string())
                            } catch (e: Exception) {
                                error = "No hay conexión con el servidor"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Midnight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text(if (loading) "Ingresando..." else "Ingresar", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Proyecto académico · David Silva y Sofía Caina",
                    fontSize = 12.sp, color = Muted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(user: LoginResponse) {
    var account by remember { mutableStateOf<AccountResponse?>(null) }
    var history by remember { mutableStateOf<List<TransactionResponse>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        try {
            val acc = ApiClient.api.account()
            if (acc.isSuccessful) account = acc.body()
            else error = parseError(acc.errorBody()?.string())
            val tx = ApiClient.api.transactions()
            if (tx.isSuccessful) history = tx.body() ?: emptyList()
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    fun send(type: String) {
        val value = amount.replace(",", ".").toDoubleOrNull()
        if (value == null || value <= 0) {
            error = "Escribe un monto válido"
            info = null
            return
        }
        loading = true
        error = null
        info = null
        scope.launch {
            try {
                val resp = ApiClient.api.movement(MovementRequest(type, value, description.trim()))
                if (resp.isSuccessful) {
                    info = resp.body()?.message
                    amount = ""
                    description = ""
                    load()
                } else {
                    error = parseError(resp.errorBody()?.string())
                }
            } catch (e: Exception) {
                error = "No hay conexión con el servidor"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    LazyColumn(
        Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row {
                        Text("Finance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Midnight)
                        Text("Tutor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Copper)
                    }
                    Text("Hola, ${user.name}", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Midnight)
                }
                Pill(user.role, Midnight, Color(0xFFE2E6EF))
            }
        }

        if (account == null && error == null) item { Loading() }

        account?.let { acc ->
            item {
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Midnight, MidnightSoft)))
                        .padding(22.dp)
                ) {
                    Column {
                        Text("SALDO DISPONIBLE", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, letterSpacing = 1.sp)
                        Text(money(acc.balance), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Copper.copy(alpha = 0.6f))
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Text("Límite diario  ", color = Color.White.copy(alpha = 0.8f))
                                Text(money(acc.dailyLimit), color = CopperLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        error?.let { msg -> item { Notice(msg, Red, RedBg) } }
        info?.let { msg -> item { Notice(msg, Green, GreenBg) } }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Nuevo movimiento", fontWeight = FontWeight.SemiBold, color = Midnight, fontSize = 17.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Monto") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Descripción") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { send("INCOME") }, enabled = !loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("Ingresar", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = { send("WITHDRAWAL") }, enabled = !loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Red),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text("Retirar", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }

        item { Text("Historial de movimientos", fontWeight = FontWeight.SemiBold, color = Midnight, fontSize = 17.sp) }
        if (history?.isEmpty() == true) item { Text("Todavía no tienes movimientos.", color = Muted) }
        items(history ?: emptyList()) { t ->
            val ingreso = t.type == "INCOME"
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        if (ingreso) Pill("Ingreso", Green, GreenBg) else Pill("Retiro", Red, RedBg)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (t.description.isNullOrBlank()) "Sin descripción" else t.description,
                            fontWeight = FontWeight.Medium, color = TextDark
                        )
                        Text(shortDate(t.createdAt), fontSize = 12.sp, color = Muted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            (if (ingreso) "+" else "-") + money(t.amount),
                            color = if (ingreso) Green else Red,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Text("Saldo ${money(t.balanceAfter)}", fontSize = 12.sp, color = Muted)
                    }
                }
            }
        }
    }
}