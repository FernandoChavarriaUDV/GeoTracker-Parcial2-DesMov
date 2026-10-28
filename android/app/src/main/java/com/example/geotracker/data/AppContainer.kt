package com.example.geotracker.data

import android.content.Context
import com.example.geotracker.BuildConfig
import com.example.geotracker.data.remote.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    val session = SessionManager(context.applicationContext)
    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(session::token))
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ApiService::class.java)
    val authRepository = AuthRepository(api, session)
    val locationRepository = LocationRepository(api)
    val locationProvider = LocationProvider(context.applicationContext)
}
