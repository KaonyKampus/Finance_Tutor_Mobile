package com.kaonykk.financetutormobile.data.remote

import com.kaonykk.financetutormobile.data.model.HealthResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//We created an object with the const for the url
// as wel we extend a method which come from the class 


object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.3:8080/FinanceTutorApi/"

    val healthApi: HealthApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HealthApi::class.java)
}