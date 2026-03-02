package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.data.model.AuthTokenResponse
import com.jinyoung.sohangseong.data.model.GoogleSocialRequest
import com.jinyoung.sohangseong.data.model.KakaoSocialRequest
import com.jinyoung.sohangseong.data.model.RefreshTokenRequest
import com.jinyoung.sohangseong.data.model.SignInRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
  @POST("auth/sign-in")
  suspend fun signIn(
    @Body request: SignInRequest
  ): AuthTokenResponse

  @POST("auth/refresh")
  suspend fun refresh(
    @Body request: RefreshTokenRequest
  ): AuthTokenResponse

  @POST("auth/social/google")
  suspend fun signInGoogle(
    @Body request: GoogleSocialRequest
  ): AuthTokenResponse

  @POST("auth/social/kakao")
  suspend fun signInKakao(
    @Body request: KakaoSocialRequest
  ): AuthTokenResponse
}
