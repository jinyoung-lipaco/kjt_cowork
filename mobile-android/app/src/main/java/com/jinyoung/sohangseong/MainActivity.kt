package com.jinyoung.sohangseong

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.jinyoung.sohangseong.data.network.ApiClient
import com.jinyoung.sohangseong.data.repository.AuthRepository
import com.jinyoung.sohangseong.data.store.TokenStore
import com.jinyoung.sohangseong.ui.auth.LoginScreen
import com.jinyoung.sohangseong.ui.auth.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private lateinit var googleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val tokenStore = TokenStore(applicationContext)
    val authApi = ApiClient.create(tokenStore)
    val authRepository = AuthRepository(authApi, tokenStore)
    val viewModel = LoginViewModel(authRepository)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .requestIdToken(getString(R.string.google_web_client_id))
      .build()
    googleSignInClient = GoogleSignIn.getClient(this, gso)

    val googleSignInLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
          val account = task.getResult(ApiException::class.java)
          val idToken = account.idToken
          if (idToken.isNullOrBlank()) {
            viewModel.setError("구글 ID 토큰을 확인할 수 없습니다. client id 설정을 확인해 주세요.")
            return@registerForActivityResult
          }
          viewModel.signInGoogle(idToken = idToken, nickname = account.displayName)
        } catch (e: Exception) {
          viewModel.setError("구글 로그인 실패: ${e.message ?: "알 수 없는 오류"}")
        }
      }

    setContent {
      val scope = rememberCoroutineScope()
      val loginState by viewModel.state.collectAsState()

      LoginScreen(
        state = loginState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClick = { viewModel.signIn() },
        onGoogleLoginClick = {
          googleSignInClient.signOut()
            .addOnCompleteListener {
              googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        },
        onKakaoLoginClick = { startKakaoLogin(viewModel) },
        onSignOutClick = {
          scope.launch {
            authRepository.signOut()
          }
        }
      )
    }
  }

  private fun startKakaoLogin(viewModel: LoginViewModel) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
      if (error != null) {
        viewModel.setError("카카오 로그인 실패: ${error.message ?: "알 수 없는 오류"}")
      } else if (token != null) {
        UserApiClient.instance.me { user, meError ->
          if (meError != null) {
            viewModel.signInKakao(token.accessToken, null)
          } else {
            viewModel.signInKakao(token.accessToken, user?.kakaoAccount?.profile?.nickname)
          }
        }
      }
    }

    if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
      UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
    } else {
      UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
    }
  }
}
