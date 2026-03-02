package com.jinyoung.sohangseong.data.store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.authDataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {
  private val accessTokenKey = stringPreferencesKey("access_token")
  private val refreshTokenKey = stringPreferencesKey("refresh_token")
  private val userIdKey = stringPreferencesKey("user_id")
  private val userNicknameKey = stringPreferencesKey("user_nickname")

  val userIdFlow: Flow<String?> = context.authDataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { prefs -> prefs[userIdKey] }

  val nicknameFlow: Flow<String?> = context.authDataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { prefs -> prefs[userNicknameKey] }

  suspend fun saveAuth(
    accessToken: String,
    refreshToken: String,
    userId: String,
    nickname: String
  ) {
    context.authDataStore.edit { prefs: MutablePreferences ->
      prefs[accessTokenKey] = accessToken
      prefs[refreshTokenKey] = refreshToken
      prefs[userIdKey] = userId
      prefs[userNicknameKey] = nickname
    }
  }

  suspend fun clear() {
    context.authDataStore.edit { prefs ->
      prefs.remove(accessTokenKey)
      prefs.remove(refreshTokenKey)
      prefs.remove(userIdKey)
      prefs.remove(userNicknameKey)
    }
  }

  suspend fun getAccessToken(): String? {
    return context.authDataStore.data.first()[accessTokenKey]
  }

  suspend fun getRefreshToken(): String? {
    return context.authDataStore.data.first()[refreshTokenKey]
  }

  suspend fun updateTokens(accessToken: String, refreshToken: String) {
    context.authDataStore.edit { prefs: MutablePreferences ->
      prefs[accessTokenKey] = accessToken
      prefs[refreshTokenKey] = refreshToken
    }
  }
}
