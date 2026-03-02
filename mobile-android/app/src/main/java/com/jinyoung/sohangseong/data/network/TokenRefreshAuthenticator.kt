package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.data.model.RefreshTokenRequest
import com.jinyoung.sohangseong.data.store.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenRefreshAuthenticator(
  private val tokenStore: TokenStore,
  baseUrl: String
) : Authenticator {
  private val refreshApi: AuthApi = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(OkHttpClient.Builder().build())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(AuthApi::class.java)

  override fun authenticate(route: Route?, response: Response): Request? {
    if (response.request.url.encodedPath.endsWith("/auth/refresh")) return null
    if (responseCount(response) >= 2) return null

    val refreshToken = runBlocking { tokenStore.getRefreshToken() } ?: return null

    val refreshed = runBlocking {
      runCatching {
        refreshApi.refresh(RefreshTokenRequest(refreshToken))
      }.getOrNull()
    } ?: run {
      runBlocking { tokenStore.clear() }
      return null
    }

    runBlocking {
      tokenStore.updateTokens(
        accessToken = refreshed.accessToken,
        refreshToken = refreshed.refreshToken
      )
    }

    return response.request.newBuilder()
      .header("Authorization", "Bearer ${refreshed.accessToken}")
      .build()
  }

  private fun responseCount(response: Response): Int {
    var current: Response? = response
    var count = 1
    while (current?.priorResponse != null) {
      count++
      current = current.priorResponse
    }
    return count
  }
}
