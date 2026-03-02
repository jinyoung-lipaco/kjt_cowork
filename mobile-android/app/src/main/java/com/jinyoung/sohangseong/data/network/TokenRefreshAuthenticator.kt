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
    if (responseCount(response) >= 2) {
      runBlocking { tokenStore.expireSession("인증이 만료되어 로그아웃되었습니다. 다시 로그인해 주세요.") }
      return null
    }

    val refreshToken = runBlocking { tokenStore.getRefreshToken() } ?: run {
      runBlocking { tokenStore.expireSession("세션 정보가 없어 로그아웃되었습니다. 다시 로그인해 주세요.") }
      return null
    }

    val refreshed = runBlocking {
      runCatching {
        refreshApi.refresh(RefreshTokenRequest(refreshToken))
      }.getOrNull()
    } ?: run {
      runBlocking { tokenStore.expireSession("세션이 만료되어 로그아웃되었습니다. 다시 로그인해 주세요.") }
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
