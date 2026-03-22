package com.example.f1project.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// --- Singleton Retrofit do wywołań API F1 ---
object RetrofitInstance {

    // Lazy-inicjalizacja API – instancja tworzona tylko przy pierwszym użyciu
    val api: F1ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(F1ApiService.BASE_URL) // Główny adres API
            .addConverterFactory(GsonConverterFactory.create()) // Konwerter JSON
            .build()
            .create(F1ApiService::class.java) // Tworzy implementację interfejsu F1ApiService
    }
}
