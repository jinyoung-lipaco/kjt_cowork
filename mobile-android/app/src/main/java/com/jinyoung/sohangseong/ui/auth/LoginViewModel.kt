package com.jinyoung.sohangseong.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinyoung.sohangseong.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
  val email: String = "",
  val password: String = "",
  val loading: Boolean = false,
  val loadingProvider: String? = null,
  val nickname: String? = null,
  val errorMessage: String? = null
)

class LoginViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {
  private val _state = MutableStateFlow(LoginUiState())
  val state: StateFlow<LoginUiState> = _state.asStateFlow()

  fun onEmailChanged(value: String) {
    _state.update { it.copy(email = value, errorMessage = null) }
  }

  fun onPasswordChanged(value: String) {
    _state.update { it.copy(password = value, errorMessage = null) }
  }

  fun signIn() {
    val current = _state.value
    if (current.email.isBlank() || current.password.isBlank()) {
      _state.update { it.copy(errorMessage = "이메일과 비밀번호를 입력해 주세요.") }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(loading = true, loadingProvider = "EMAIL", errorMessage = null) }
      authRepository.signIn(current.email, current.password)
        .onSuccess { response ->
          _state.update {
            it.copy(
              loading = false,
              loadingProvider = null,
              nickname = response.user.nickname,
              password = ""
            )
          }
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              loadingProvider = null,
              errorMessage = "로그인 실패: ${error.message ?: "알 수 없는 오류"}"
            )
          }
        }
    }
  }

  fun signInGoogle(idToken: String, nickname: String? = null) {
    socialSignIn(
      provider = "GOOGLE",
      call = { authRepository.signInWithGoogle(idToken, nickname) }
    )
  }

  fun signInKakao(accessToken: String, nickname: String? = null) {
    socialSignIn(
      provider = "KAKAO",
      call = { authRepository.signInWithKakao(accessToken, nickname) }
    )
  }

  private fun socialSignIn(
    provider: String,
    call: suspend () -> Result<com.jinyoung.sohangseong.data.model.AuthTokenResponse>
  ) {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, loadingProvider = provider, errorMessage = null) }
      call()
        .onSuccess { response ->
          _state.update {
            it.copy(
              loading = false,
              loadingProvider = null,
              nickname = response.user.nickname
            )
          }
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              loadingProvider = null,
              errorMessage = "소셜 로그인 실패: ${error.message ?: "알 수 없는 오류"}"
            )
          }
        }
    }
  }

  fun setError(message: String) {
    _state.update { it.copy(errorMessage = message, loading = false, loadingProvider = null) }
  }
}
