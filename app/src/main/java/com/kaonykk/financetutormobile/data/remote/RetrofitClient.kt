package com.kaonykk.financetutormobile.data.remote

import com.kaonykk.financetutormobile.data.model.HealthResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.3:8080/FinanceTutorApi/"

    val healthApi: HealthApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HealthApi::class.java)
}