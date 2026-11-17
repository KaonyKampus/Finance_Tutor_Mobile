package com.kaonykk.financetutormobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.kaonykk.financetutormobile.data.model.MessageResponse
import java.util.Locale

val Midnight = Color(0xFF1E2A4A)
val MidnightSoft = Color(0xFF2C3B63)
val Copper = Color(0xFFB87333)
val CopperLight = Color(0xFFF2C79E)
val Bg = Color(0xFFF4F6FA)
val Muted = Color(0xFF6B7285)
val TextDark = Color(0xFF1F2433)
val Green = Color(0xFF1E8A5A)
val GreenBg = Color(0xFFE5F5EC)
val Red = Color(0xFFC0392B)
val RedBg = Color(0xFFFBEAE8)
val Amber = Color(0xFFA86B0C)
val AmberBg = Color(0xFFFCF1DD)

@Composable
fun Pill(text: String, fg: Color, bg: Color) {
    Text(
        text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
fun StatusPill(status: String) {
    when (status) {
        "APPROVED" -> Pill("Aprobada", Green, GreenBg)
        "REJECTED" -> Pill("Rechazada", Red, RedBg)
        else -> Pill("Pendiente", Amber, AmberBg)
    }
}

@Composable
fun Notice(text: String, fg: Color, bg: Color) {
    Text(
        text, color = fg,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg).padding(12.dp)
    )
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Midnight)
        if (subtitle != null) Text(subtitle, color = Muted)
    }
}

@Composable
fun Loading() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Copper)
        Spacer(Modifier.width(10.dp))
        Text("Cargando...", color = Muted)
    }
}

fun money(v: Double): String = "$" + String.format(Locale("es", "CO"), "%,.0f", v)

fun shortDate(iso: String): String = iso.replace("T", " ").take(16)

fun parseError(json: String?): String =
    try {
        Gson().fromJson(json, MessageResponse::class.java)?.error ?: "Ocurrió un error"
    } catch (e: Exception) {
        "Ocurrió un error"
    }