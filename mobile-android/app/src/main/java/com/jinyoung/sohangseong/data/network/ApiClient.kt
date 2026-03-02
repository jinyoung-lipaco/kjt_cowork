package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.data.store.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
  // Android emulator local host mapping.
  const val BASE_URL = "http://10.0.2.2:4000/api/"

  fun create(tokenStore: TokenStore): AuthApi {
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
      .create(AuthApi::class.java)
  }
}
