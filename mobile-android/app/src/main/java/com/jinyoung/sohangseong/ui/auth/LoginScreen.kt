package com.jinyoung.sohangseong.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jinyoung.sohangseong.ui.common.AppSnackbarHost
import com.jinyoung.sohangseong.ui.common.AppSnackbarType
import com.jinyoung.sohangseong.ui.common.AppSnackbarVisuals

@Composable
fun LoginScreen(
  state: LoginUiState,
  onEmailChanged: (String) -> Unit,
  onPasswordChanged: (String) -> Unit,
  onLoginClick: () -> Unit,
  onGoogleLoginClick: () -> Unit,
  onKakaoLoginClick: () -> Unit,
  onSignOutClick: () -> Unit,
  onConsumeErrorMessage: () -> Unit,
  onConsumeInfoMessage: () -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(state.errorMessage) {
    val message = state.errorMessage ?: return@LaunchedEffect
    snackbarHostState.showSnackbar(
      AppSnackbarVisuals(
        message = message,
        type = AppSnackbarType.ERROR,
        duration = SnackbarDuration.Long
      )
    )
    onConsumeErrorMessage()
  }

  LaunchedEffect(state.infoMessage) {
    val message = state.infoMessage ?: return@LaunchedEffect
    snackbarHostState.showSnackbar(
      AppSnackbarVisuals(
        message = message,
        type = AppSnackbarType.INFO,
        duration = SnackbarDuration.Short
      )
    )
    onConsumeInfoMessage()
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Scaffold(
      snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(PaddingValues(horizontal = 20.dp, vertical = 32.dp)),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "소행성 로그인",
          style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "이메일 + 카카오 + 구글 로그인 연동",
          style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
          value = state.email,
          onValueChange = onEmailChanged,
          label = { Text("이메일") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = state.password,
          onValueChange = onPasswordChanged,
          label = { Text("비밀번호") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onLoginClick,
          enabled = !state.loading,
          modifier = Modifier.fillMaxWidth()
        ) {
          if (state.loading && state.loadingProvider == "EMAIL") {
            CircularProgressIndicator(
              strokeWidth = 2.dp,
              modifier = Modifier.height(18.dp)
            )
          } else {
            Text("로그인")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onKakaoLoginClick,
            enabled = !state.loading,
            modifier = Modifier.weight(1f)
          ) {
            if (state.loading && state.loadingProvider == "KAKAO") {
              CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(18.dp))
            } else {
              Text("카카오 로그인")
            }
          }

          Button(
            onClick = onGoogleLoginClick,
            enabled = !state.loading,
            modifier = Modifier.weight(1f)
          ) {
            if (state.loading && state.loadingProvider == "GOOGLE") {
              CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(18.dp))
            } else {
              Text("구글 로그인")
            }
          }
        }

        if (state.nickname != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "환영합니다, ${state.nickname}님",
            style = MaterialTheme.typography.bodyLarge
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("로그아웃")
          }
        }
      }
    }
  }
}
