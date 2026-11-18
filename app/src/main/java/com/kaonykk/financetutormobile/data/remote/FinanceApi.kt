package com.kaonykk.financetutormobile.data.remote

import com.kaonykk.financetutormobile.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

    @GET("api/wards")
    suspend fun wards(): Response<List<AccountResponse>>

    @POST("api/wards")
    suspend fun updateLimit(@Body body: LimitRequest): Response<MessageResponse>

    @GET("api/admin/users")
    suspend fun users(@Query("role") role: String): Response<List<UserResponse>>

    @POST("api/admin/users")
    suspend fun register(@Body body: RegisterRequest): Response<MessageResponse>

    @POST("api/admin/assignments")
    suspend fun assign(@Body body: AssignmentRequest): Response<MessageResponse>
}