package com.jinyoung.sohangseong.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarVisuals

enum class AppSnackbarType {
  ERROR,
  INFO,
  SUCCESS
}

data class AppSnackbarVisuals(
  override val message: String,
  val type: AppSnackbarType,
  override val actionLabel: String? = null,
  override val withDismissAction: Boolean = false,
  override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState) {
  SnackbarHost(hostState = hostState) { data ->
    AppSnackbar(data = data)
  }
}

@Composable
private fun AppSnackbar(data: SnackbarData) {
  val visuals = data.visuals as? AppSnackbarVisuals
  val type = visuals?.type ?: AppSnackbarType.INFO

  val containerColor = when (type) {
    AppSnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer
    AppSnackbarType.INFO -> MaterialTheme.colorScheme.secondaryContainer
    AppSnackbarType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
  }
  val contentColor = if (containerColor.luminance() > 0.5f) Color.Black else Color.White

  Snackbar(
    snackbarData = data,
    containerColor = containerColor,
    contentColor = contentColor,
    actionColor = contentColor,
    shape = MaterialTheme.shapes.medium,
    tonalElevation = 2.dp
  )
}
