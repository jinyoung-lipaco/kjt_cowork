package com.jinyoung.sohangseong

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.jinyoung.sohangseong.data.network.ApiClient
import com.jinyoung.sohangseong.data.repository.AuthRepository
import com.jinyoung.sohangseong.data.repository.MainRepository
import com.jinyoung.sohangseong.data.store.TokenStore
import com.jinyoung.sohangseong.ui.auth.LoginScreen
import com.jinyoung.sohangseong.ui.auth.LoginViewModel
import com.jinyoung.sohangseong.ui.main.MainTabsScreen
import com.jinyoung.sohangseong.ui.main.MainTabsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private lateinit var googleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val tokenStore = TokenStore(applicationContext)
    val authApi = ApiClient.createAuthApi(tokenStore)
    val mainApi = ApiClient.createMainApi(tokenStore)
    val authRepository = AuthRepository(authApi, tokenStore)
    val mainRepository = MainRepository(mainApi)
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
        } catch (e: ApiException) {
          when (e.statusCode) {
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
              viewModel.setInfo("구글 로그인을 취소했어요.")
            }
            CommonStatusCodes.SIGN_IN_REQUIRED -> {
              viewModel.setError("구글 계정 선택이 필요합니다.")
            }
            CommonStatusCodes.NETWORK_ERROR -> {
              viewModel.setError("네트워크 문제로 구글 로그인에 실패했습니다.")
            }
            else -> {
              viewModel.setError("구글 로그인 실패: ${e.localizedMessage ?: "알 수 없는 오류"}")
            }
          }
        } catch (e: Exception) {
          viewModel.setError("구글 로그인 실패: ${e.localizedMessage ?: "알 수 없는 오류"}")
        }
      }

    setContent {
      val scope = rememberCoroutineScope()
      val loginState by viewModel.state.collectAsState()
      val savedUserId by tokenStore.userIdFlow.collectAsState(initial = null)
      val savedNickname by tokenStore.nicknameFlow.collectAsState(initial = null)
      val sessionExpiredMessage by tokenStore.sessionExpiredMessageFlow.collectAsState(initial = null)
      val mainTabsViewModel = remember { MainTabsViewModel(mainRepository) }
      val mainTabsState by mainTabsViewModel.state.collectAsState()

      LaunchedEffect(savedUserId, savedNickname, sessionExpiredMessage) {
        if (savedUserId == null && savedNickname == null && !sessionExpiredMessage.isNullOrBlank()) {
          viewModel.setInfo(sessionExpiredMessage ?: "")
          tokenStore.clearSessionExpiredMessage()
        }
      }

      if (savedUserId == null || savedNickname == null) {
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
      } else {
        LaunchedEffect(savedUserId, savedNickname) {
          mainTabsViewModel.refresh(savedUserId ?: "")
        }
        MainTabsScreen(
          state = mainTabsState,
          userId = savedUserId ?: "",
          nickname = savedNickname ?: "",
          onSelectTab = mainTabsViewModel::selectTab,
          onRefresh = { mainTabsViewModel.refresh(savedUserId ?: "") },
          onCreatePost = { title, body ->
            mainTabsViewModel.createPost(savedUserId ?: "", title, body)
          },
          onCreateComment = { postId, body ->
            mainTabsViewModel.createComment(savedUserId ?: "", postId, body)
          },
          onVote = { pollId, optionId ->
            mainTabsViewModel.vote(savedUserId ?: "", pollId, optionId)
          },
          onSignOut = {
            scope.launch {
              authRepository.signOut()
            }
          }
        }
      }
    }
  }

  private fun startKakaoLogin(viewModel: LoginViewModel) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
      if (error != null) {
        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
          viewModel.setInfo("카카오 로그인을 취소했어요.")
        } else {
          viewModel.setError("카카오 로그인 실패: ${error.localizedMessage ?: "알 수 없는 오류"}")
        }
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
