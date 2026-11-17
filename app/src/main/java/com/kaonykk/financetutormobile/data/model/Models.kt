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

data class MovementRequest(val type: String, val amount: Double, val description: String)

data class RequestResponse(
    val id: Long,
    val requestedBy: String,
    val amount: Double,
    val status: String,
    val createdAt: String
)
data class DecisionRequest(val requestId: Long, val action: String)

data class MessageResponse(val message: String?, val error: String?)