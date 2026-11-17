package com.kaonykk.financetutormobile.data.remote

import com.kaonykk.financetutormobile.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FinanceApi {
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/logout")
    suspend fun logout(): Response<MessageResponse>

    @GET("api/account")
    suspend fun account(): Response<AccountResponse>

    @GET("api/transactions")
    suspend fun transactions(): Response<List<TransactionResponse>>

    @POST("api/movements")
    suspend fun movement(@Body body: MovementRequest): Response<MessageResponse>

    @GET("api/requests")
    suspend fun requests(): Response<List<RequestResponse>>

    @POST("api/requests")
    suspend fun decide(@Body body: DecisionRequest): Response<MessageResponse>
}