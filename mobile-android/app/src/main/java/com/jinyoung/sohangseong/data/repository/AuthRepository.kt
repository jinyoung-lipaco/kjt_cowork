package com.jinyoung.sohangseong.data.repository

import com.jinyoung.sohangseong.data.model.AuthTokenResponse
import com.jinyoung.sohangseong.data.model.GoogleSocialRequest
import com.jinyoung.sohangseong.data.model.KakaoSocialRequest
import com.jinyoung.sohangseong.data.model.SignInRequest
import com.jinyoung.sohangseong.data.network.AuthApi
import com.jinyoung.sohangseong.data.store.TokenStore

class AuthRepository(
  private val authApi: AuthApi,
  private val tokenStore: TokenStore
) {
  suspend fun signIn(email: String, password: String): Result<AuthTokenResponse> {
    return runCatching {
      val response = authApi.signIn(SignInRequest(email = email, password = password))
      saveAuthResult(response)
      response
    }
  }

  suspend fun signInWithGoogle(idToken: String, nickname: String? = null): Result<AuthTokenResponse> {
    return runCatching {
      val response = authApi.signInGoogle(
        GoogleSocialRequest(idToken = idToken, nickname = nickname)
      )
      saveAuthResult(response)
      response
    }
  }

  suspend fun signInWithKakao(accessToken: String, nickname: String? = null): Result<AuthTokenResponse> {
    return runCatching {
      val response = authApi.signInKakao(
        KakaoSocialRequest(accessToken = accessToken, nickname = nickname)
      )
      saveAuthResult(response)
      response
    }
  }

  suspend fun signOut() {
    tokenStore.clear()
  }

  private suspend fun saveAuthResult(response: AuthTokenResponse) {
    tokenStore.saveAuth(
      accessToken = response.accessToken,
      refreshToken = response.refreshToken,
      nickname = response.user.nickname
    )
  }
}
