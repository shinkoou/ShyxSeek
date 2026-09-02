package com.shyxseek.app.di

import okhttp3.OkHttpClient
import retrofit2.Retrofit

/** Retrofit is available for typed connector APIs; streaming chat uses OkHttp directly for SSE. */
class RetrofitFactory(private val client: OkHttpClient) {
    fun create(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.trimEnd('/') + "/")
        .client(client)
        .build()
}
