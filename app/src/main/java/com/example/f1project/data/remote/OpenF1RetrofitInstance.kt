package com.example.f1project.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Osobna instancja Retrofit dla OpenF1 — inny base URL niż Jolpica
object OpenF1RetrofitInstance {

    val api: OpenF1ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OpenF1ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenF1ApiService::class.java)
    }
}