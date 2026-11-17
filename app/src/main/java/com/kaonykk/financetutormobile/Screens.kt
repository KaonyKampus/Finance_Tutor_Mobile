package com.kaonykk.financetutormobile

import com.kaonykk.financetutormobile.data.model.*
import com.kaonykk.financetutormobile.data.remote.ApiClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LoginScreen(onLogin: (LoginResponse) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("FinanceTutor", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Correo") }, singleLine = true,
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
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                loading = true
                error = null
                scope.launch {
                    try {
                        val resp = ApiClient.api.login(LoginRequest(email.trim(), password))
                        val body = resp.body()
                        if (resp.isSuccessful && body != null) onLogin(body)
                        else error = if (resp.code() == 401) "Correo o contraseña incorrectos"
                        else "Completa todos los campos"
                    } catch (e: Exception) {
                        error = "No hay conexión con el servidor"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (loading) "Ingresando..." else "Ingresar") }
    }
}

@Composable
fun HomeScreen(user: LoginResponse) {
    var account by remember { mutableStateOf<AccountResponse?>(null) }
    var history by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val acc = ApiClient.api.account()
            if (acc.isSuccessful) account = acc.body()
            else error = "No se pudo cargar la cuenta (${acc.code()})"
            val tx = ApiClient.api.transactions()
            if (tx.isSuccessful) history = tx.body() ?: emptyList()
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Hola, ${user.name}", style = MaterialTheme.typography.headlineSmall) }
        error?.let { msg -> item { Text(msg, color = MaterialTheme.colorScheme.error) } }
        account?.let { acc ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Saldo disponible")
                        Text(money(acc.balance), style = MaterialTheme.typography.headlineLarge)
                        Text("Límite diario: ${money(acc.dailyLimit)}")
                    }
                }
            }
        }
        item { Text("Historial", style = MaterialTheme.typography.titleMedium) }
        if (history.isEmpty()) item { Text("Todavía no tienes movimientos.") }
        items(history) { t ->
            val ingreso = t.type == "INCOME"
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(t.description ?: if (ingreso) "Ingreso" else "Retiro")
                        Text(t.createdAt.replace("T", " ").take(16), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        (if (ingreso) "+" else "-") + money(t.amount),
                        color = if (ingreso) Color(0xFF1E8A5A) else Color(0xFFC0392B)
                    )
                }
            }
        }
    }
}

fun money(v: Double): String = "$" + String.format(Locale("es", "CO"), "%,.0f", v)