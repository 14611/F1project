package com.example.f1project.data.remote

import android.content.Context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val CACHE_SIZE_BYTES        = 10L * 1024L * 1024L
    private const val MAX_AGE_ONLINE_SECONDS  = 300        // 5 minut — normalny cache
    private const val MAX_STALE_OFFLINE_DAYS  = 604_800    // 7 dni — offline fallback
    private const val MAX_STALE_WIDGET_DAYS   = 1          // 1 dzień — widget cache-only

    private lateinit var appContext: Context

    private val sharedCache: Cache by lazy {
        Cache(File(appContext.cacheDir, "http_cache_f1"), CACHE_SIZE_BYTES)
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // ── Normalny klient: sieć jeśli cache > 5 min ────────────────────────────
    val api: F1ApiService by lazy {
        buildRetrofit(mode = ClientMode.NORMAL).create(F1ApiService::class.java)
    }

    // ── Force-refresh: zawsze sieć (pull-to-refresh) ─────────────────────────
    val apiRefresh: F1ApiService by lazy {
        buildRetrofit(mode = ClientMode.FORCE_NETWORK).create(F1ApiService::class.java)
    }

    // ── Cache-only: wyłącznie dysk OkHttp, zero sieci ────────────────────────
    // Jeśli cache pusty → 504 Gateway Timeout → RepositoryResult.Error
    // Używany przez widget — nigdy nie generuje ruchu sieciowego
    val apiCacheOnly: F1ApiService by lazy {
        buildRetrofit(mode = ClientMode.CACHE_ONLY).create(F1ApiService::class.java)
    }

    // ─────────────────────────────────────────────────────────────────────────

    private enum class ClientMode { NORMAL, FORCE_NETWORK, CACHE_ONLY }

    private fun buildRetrofit(mode: ClientMode): Retrofit {

        // Application interceptor — modyfikuje żądanie wychodzące
        val requestInterceptor = Interceptor { chain ->
            var request = chain.request()
            request = when (mode) {

                ClientMode.CACHE_ONLY -> {
                    // Nigdy nie wychodź do sieci; akceptuj dane do 1 dnia wstecz
                    request.newBuilder()
                        .cacheControl(
                            CacheControl.Builder()
                                .onlyIfCached()
                                .maxStale(MAX_STALE_WIDGET_DAYS, TimeUnit.DAYS)
                                .build()
                        )
                        .build()
                }

                ClientMode.FORCE_NETWORK -> {
                    // Ignoruj cache, bezwzględnie idź do sieci
                    request.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build()
                }

                ClientMode.NORMAL -> {
                    if (!NetworkUtils.isOnline(appContext)) {
                        // Offline: serwuj z dysku do 7 dni wstecz
                        request.newBuilder()
                            .header(
                                "Cache-Control",
                                "public, only-if-cached, max-stale=$MAX_STALE_OFFLINE_DAYS"
                            )
                            .build()
                    } else {
                        request // Bez modyfikacji — OkHttp sam waliduje max-age
                    }
                }
            }
            chain.proceed(request)
        }

        // Network interceptor — wstrzykuje Cache-Control w odpowiedź serwera
        // Nie dodajemy dla CACHE_ONLY — ten klient nigdy nie wychodzi do sieci
        val writeCacheInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header("Cache-Control", "public, max-age=$MAX_AGE_ONLINE_SECONDS")
                .removeHeader("Pragma")
                .build()
        }

        val clientBuilder = OkHttpClient.Builder()
            .cache(sharedCache)
            .addInterceptor(requestInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)

        // Network interceptor tylko dla klientów które faktycznie wychodzą do sieci
        if (mode != ClientMode.CACHE_ONLY) {
            clientBuilder.addNetworkInterceptor(writeCacheInterceptor)
        }

        return Retrofit.Builder()
            .baseUrl(F1ApiService.BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}