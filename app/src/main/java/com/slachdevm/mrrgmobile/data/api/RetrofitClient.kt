package com.slachdevm.mrrgmobile.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.slachdevm.mrrgmobile.BuildConfig
import com.slachdevm.mrrgmobile.data.util.SessionExpiredException

object RetrofitClient {
    const val BASE_URL = BuildConfig.BASE_URL
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "

    private var authToken: String? = null
    private var onAuthErrorListener: (() -> Unit)? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setOnAuthErrorListener(listener: () -> Unit) {
        onAuthErrorListener = listener
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        authToken?.let {
            requestBuilder.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + it)
        }

        val response = chain.proceed(requestBuilder.build())
        
        if ((response.code == 401 || response.code == 403) && authToken != null) {
            onAuthErrorListener?.invoke()
            throw SessionExpiredException()
        }
        
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val jobApi: JobApi by lazy {
        retrofit.create(JobApi::class.java)
    }

    val notificationApi: NotificationApi by lazy {
        retrofit.create(NotificationApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }
}
