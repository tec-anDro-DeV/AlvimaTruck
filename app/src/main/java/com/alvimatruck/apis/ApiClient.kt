package com.alvimatruck.apis

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient private constructor(
    baseURL: String, token: String
) {
    var webservices: ApiInterface

    companion object {
        private var restClient: ApiClient? = null
        fun getRestClient(
            url: String, token: String
        ): ApiClient? {
            try {
                restClient = ApiClient(url, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return restClient
        }
    }

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient =
            OkHttpClient.Builder().connectTimeout(100, TimeUnit.SECONDS).addInterceptor(logging)
                .addNetworkInterceptor { chain: Interceptor.Chain ->
                    val request = chain.request().newBuilder()
                    request.method(chain.request().method, chain.request().body)
                    if (token.trim().isNotEmpty()) {
                        request.addHeader("Authorization", "Bearer $token")
                    }
                    request.build()
                    Log.d("TAG", request.build().body.toString())
                    chain.proceed(request.build())
                }.readTimeout(100, TimeUnit.SECONDS).build()
        val retrofit =
            Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create())
                .client(httpClient).build()
        webservices = retrofit.create(ApiInterface::class.java)
    }
}