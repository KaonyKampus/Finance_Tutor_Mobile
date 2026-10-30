package com.kaonykk.financetutormobile.data.remote

import com.kaonykk.financetutormobile.data.model.HealthResponse
import retrofit2.http.GET

interface HealthApi {
    @GET("api/health")
    suspend fun healthApi(): HealthResponse
}