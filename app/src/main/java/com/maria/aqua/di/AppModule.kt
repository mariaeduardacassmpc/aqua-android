package com.maria.aqua.di

import com.maria.aqua.SessionManager
import com.maria.aqua.data.remote.AuthApi
import com.maria.aqua.data.remote.WaterApi
import com.maria.aqua.data.repository.WaterRepository
import com.maria.aqua.ui.main.MainViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single<OkHttpClient>(qualifier = named("auth")) {
        OkHttpClient.Builder().build()
    }

    single<OkHttpClient>(qualifier = named("authenticated")) {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val token = SessionManager.getToken(androidContext())
                val request = chain.request().newBuilder()

                if (!token.isNullOrBlank()) {
                    request.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(request.build())
            })
            .build()
    }

    single<Retrofit>(qualifier = named("auth")) {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5152/")
            .client(get(qualifier = named("auth")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<Retrofit>(qualifier = named("authenticated")) {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5152/")
            .client(get(qualifier = named("authenticated")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AuthApi> {
        get<Retrofit>(qualifier = named("auth"))
            .create(AuthApi::class.java)
    }

    single<WaterApi> {
        get<Retrofit>(qualifier = named("authenticated"))
            .create(WaterApi::class.java)
    }

    single { WaterRepository(get()) }

    viewModel { MainViewModel(get()) }
}
