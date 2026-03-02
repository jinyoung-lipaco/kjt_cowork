package com.jinyoung.sohangseong.data.model

data class SignInRequest(
  val email: String,
  val password: String
)

data class GoogleSocialRequest(
  val idToken: String,
  val nickname: String? = null
)

data class KakaoSocialRequest(
  val accessToken: String,
  val nickname: String? = null
)

data class RefreshTokenRequest(
  val refreshToken: String
)

data class AuthUserDto(
  val id: String,
  val email: String,
  val nickname: String,
  val tier: String
)

data class AuthTokenResponse(
  val user: AuthUserDto,
  val accessToken: String,
  val refreshToken: String
)
