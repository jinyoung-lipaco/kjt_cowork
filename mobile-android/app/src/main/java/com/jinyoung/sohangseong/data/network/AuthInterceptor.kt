package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.data.store.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import kotlinx.coroutines.runBlocking

class AuthInterceptor(
  private val tokenStore: TokenStore
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val token = runBlocking { tokenStore.getAccessToken() }
    val requestBuilder = chain.request().newBuilder()

    if (!token.isNullOrBlank()) {
      requestBuilder.addHeader("Authorization", "Bearer $token")
    }

    return chain.proceed(requestBuilder.build())
  }
}
