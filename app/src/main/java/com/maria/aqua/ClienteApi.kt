
package com.maria.aqua

import android.content.Context
import com.maria.aqua.data.remote.WaterApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClienteApi {
    private const val BASE_URL = "http://10.0.2.2:5152/"

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val authClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val authenticatedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val token = SessionManager.getToken(appContext)
                val request = chain.request().newBuilder()

                if (!token.isNullOrBlank()) {
                    request.addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                chain.proceed(request.build())
            })
            .build()
    }

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        createRetrofit(authClient).create(AuthApi::class.java)
    }

    val waterApi: WaterApi by lazy {
        createRetrofit(authenticatedClient).create(WaterApi::class.java)
    }
}
