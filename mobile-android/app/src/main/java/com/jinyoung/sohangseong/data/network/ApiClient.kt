package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.BuildConfig
import com.jinyoung.sohangseong.data.store.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
  val BASE_URL: String = BuildConfig.API_BASE_URL

  fun createAuthApi(tokenStore: TokenStore): AuthApi {
    return buildRetrofit(tokenStore).create(AuthApi::class.java)
  }

  fun createMainApi(tokenStore: TokenStore): MainApi {
    return buildRetrofit(tokenStore).create(MainApi::class.java)
  }

  private fun buildRetrofit(tokenStore: TokenStore): Retrofit {
    val logging = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }

    val authInterceptor = AuthInterceptor(tokenStore)
    val tokenAuthenticator = TokenRefreshAuthenticator(tokenStore, BASE_URL)

    val client = OkHttpClient.Builder()
      .addInterceptor(authInterceptor)
      .authenticator(tokenAuthenticator)
      .addInterceptor(logging)
      .build()

    return Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }
}
