package com.kaonykk.financetutormobile.data.remote

import com.kaonykk.financetutormobile.data.model.HealthResponse
import retrofit2.http.GET

//Get method to get information from the URL. It also returns a response
interface HealthApi {
    @GET("api/health")
    suspend fun getHealth(): HealthResponse
}