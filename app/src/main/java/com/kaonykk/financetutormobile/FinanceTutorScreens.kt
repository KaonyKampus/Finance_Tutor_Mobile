package com.kaonykk.financetutormobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun BudgetsScreen() {
    var wards by remember { mutableStateOf<List<AccountResponse>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val inputs = remember { mutableStateMapOf<Long, String>() }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        try {
            val r = ApiClient.api.wards()
            if (r.isSuccessful) wards = r.body() ?: emptyList()
            else error = parseError(r.errorBody()?.string())
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    fun save(acc: AccountResponse) {
        val value = inputs[acc.id]?.replace(",", ".")?.toDoubleOrNull()
        if (value == null || value < 0) {
            error = "Escribe un límite válido para ${acc.ownerName}"
            info = null
            return
        }
        busy = true
        error = null
        info = null
        scope.launch {
            try {
                val r = ApiClient.api.updateLimit(LimitRequest(acc.id, value))
                if (r.isSuccessful) {
                    info = "${r.body()?.message}: ${acc.ownerName} ahora tiene ${money(value)}"
                    inputs.remove(acc.id)
                    load()
                } else {
                    error = parseError(r.errorBody()?.string())
                }
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
        item { ScreenTitle("Presupuestos", "Define el límite diario de retiro de tus tutelados") }
        error?.let { msg -> item { Notice(msg, Red, RedBg) } }
        info?.let { msg -> item { Notice(msg, Green, GreenBg) } }
        if (wards == null && error == null) item { Loading() }
        if (wards?.isEmpty() == true) item { Text("Todavía no tienes tutelados vinculados.", color = Muted) }
        items(wards ?: emptyList()) { acc ->
            Panel {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(acc.ownerName, fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 16.sp)
                            Text("Saldo ${money(acc.balance)}", fontSize = 13.sp, color = Muted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Límite actual", fontSize = 12.sp, color = Muted)
                            Text(money(acc.dailyLimit), fontWeight = FontWeight.Bold, color = Copper, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputs[acc.id] ?: "",
                            onValueChange = { inputs[acc.id] = it },
                            label = { Text("Nuevo límite") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { save(acc) }, enabled = !busy,
                            colors = ButtonDefaults.buttonColors(containerColor = Midnight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp)
                        ) { Text("Guardar", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterUserScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("PRINCIPAL") }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val roles = listOf(
        "PRINCIPAL" to "Usuario principal",
        "TUTOR" to "Tutor",
        "ADMIN" to "Administrador"
    )

    fun submit() {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            error = "Completa todos los campos"
            info = null
            return
        }
        if (password.length < 8) {
            error = "La contraseña debe tener mínimo 8 caracteres"
            info = null
            return
        }
        busy = true
        error = null
        info = null
        scope.launch {
            try {
                val r = ApiClient.api.register(RegisterRequest(name.trim(), email.trim(), password, role))
                if (r.isSuccessful) {
                    info = "${r.body()?.message}: ${name.trim()}"
                    name = ""
                    email = ""
                    password = ""
                    role = "PRINCIPAL"
                } else {
                    error = parseError(r.errorBody()?.string())
                }
            } catch (e: Exception) {
                error = "No hay conexión con el servidor"
            } finally {
                busy = false
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Registrar usuario", "Crea cuentas para usuarios, tutores y administradores")
        error?.let { Notice(it, Red, RedBg) }
        info?.let { Notice(it, Green, GreenBg) }
        Panel {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Correo") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Contraseña (mínimo 8)") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Rol", fontWeight = FontWeight.SemiBold, color = Muted)
                roles.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { role = value },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = role == value, onClick = { role = value })
                        Text(label, color = TextDark)
                    }
                }
                Button(
                    onClick = { submit() }, enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = Midnight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text(if (busy) "Registrando..." else "Registrar", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
fun AssignScreen() {
    var tutors by remember { mutableStateOf<List<UserResponse>?>(null) }
    var principals by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var tutorId by remember { mutableStateOf<Long?>(null) }
    var principalId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val t = ApiClient.api.users("TUTOR")
            val p = ApiClient.api.users("PRINCIPAL")
            if (t.isSuccessful && p.isSuccessful) {
                tutors = t.body() ?: emptyList()
                principals = p.body() ?: emptyList()
            } else {
                error = parseError((if (!t.isSuccessful) t else p).errorBody()?.string())
            }
        } catch (e: Exception) {
            error = "No hay conexión con el servidor"
        }
    }

    fun submit() {
        val t = tutorId
        val p = principalId
        if (t == null || p == null) {
            error = "Elige un tutor y un usuario principal"
            info = null
            return
        }
        busy = true
        error = null
        info = null
        scope.launch {
            try {
                val r = ApiClient.api.assign(AssignmentRequest(t, p))
                if (r.isSuccessful) info = r.body()?.message
                else error = parseError(r.errorBody()?.string())
            } catch (e: Exception) {
                error = "No hay conexión con el servidor"
            } finally {
                busy = false
            }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle("Vincular tutor", "Un usuario principal puede tener varios tutores") }
        error?.let { msg -> item { Notice(msg, Red, RedBg) } }
        info?.let { msg -> item { Notice(msg, Green, GreenBg) } }
        if (tutors == null && error == null) item { Loading() }
        if (tutors != null) {
            item { Text("1. Elige el tutor", fontWeight = FontWeight.SemiBold, color = Midnight, fontSize = 16.sp) }
            if (tutors?.isEmpty() == true) item { Text("No hay tutores registrados.", color = Muted) }
            items(tutors ?: emptyList()) { u ->
                UserOption(u, tutorId == u.id) { tutorId = u.id }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Text("2. Elige el usuario principal", fontWeight = FontWeight.SemiBold, color = Midnight, fontSize = 16.sp)
            }
            if (principals.isEmpty()) item { Text("No hay usuarios principales registrados.", color = Muted) }
            items(principals) { u ->
                UserOption(u, principalId == u.id) { principalId = u.id }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { submit() }, enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = Copper),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text(if (busy) "Vinculando..." else "Vincular", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun UserOption(user: UserResponse, selected: Boolean, onSelect: () -> Unit) {
    Panel(Modifier.fillMaxWidth().clickable { onSelect() }) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column {
                Text(user.name, fontWeight = FontWeight.Medium, color = TextDark)
                Text(user.email, fontSize = 12.sp, color = Muted)
            }
        }
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier.fillMaxWidth(), content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier,
        content = content
    )
}