package com.kaonykk.financetutormobile.data.model

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val id: Long, val name: String, val role: String)
data class AccountResponse(val id: Long, val ownerName: String, val balance: Double, val dailyLimit: Double)
data class TransactionResponse(
    val id: Long,
    val type: String,
    val amount: Double,
    val description: String?,
    val balanceAfter: Double,
    val createdAt: String
)