package com.kaonykk.financetutormobile.data.remote

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy

object ApiClient {
    // Emulador: 10.0.2.2  ·  S23 físico: IP del Mac
    private const val BASE_URL = "http://192.168.0.5:8080/FinanceTutorApi/"

    private val cookieManager = CookieManager().apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) }

    private val client = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        .build()

    val api: FinanceApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FinanceApi::class.java)
}