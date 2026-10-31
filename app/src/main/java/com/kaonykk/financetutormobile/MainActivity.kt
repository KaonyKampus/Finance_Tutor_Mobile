package com.kaonykk.financetutormobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.kaonykk.financetutormobile.data.remote.RetrofitClient
import com.kaonykk.financetutormobile.ui.theme.FinanceTutorMobileTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTutorMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HealthScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
    fun HealthScreen(modifier: Modifier = Modifier) {
        var message by remember { mutableStateOf("Conectando...") }
        LaunchedEffect(Unit) {
            try{
                val response = RetrofitClient.healthApi.getHealth()
                message = "Servidor: ${response.status} - ${response.service}"
            }catch(e: Exception){
                message = "Error: ${e.message}"
            }
        }
        Text(
            text = message,
            modifier = modifier
        )
    }

