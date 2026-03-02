package com.jinyoung.sohangseong.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.VotePollDto
import com.jinyoung.sohangseong.data.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
  COMMUNITY, STANDARDS, PROFILE
}

data class MainTabsUiState(
  val selectedTab: MainTab = MainTab.COMMUNITY,
  val loading: Boolean = false,
  val errorMessage: String? = null,
  val actionMessage: String? = null,
  val posts: List<CommunityPostDto> = emptyList(),
  val polls: List<VotePollDto> = emptyList(),
  val approvedItems: List<ApprovedItemDto> = emptyList()
)

class MainTabsViewModel(
  private val repository: MainRepository
) : ViewModel() {
  private val _state = MutableStateFlow(MainTabsUiState())
  val state: StateFlow<MainTabsUiState> = _state.asStateFlow()

  init {
    refresh()
  }

  fun selectTab(tab: MainTab) {
    _state.update { it.copy(selectedTab = tab) }
  }

  fun refresh() {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null) }

      val postsResult = repository.getPosts()
      val pollsResult = repository.getPolls()
      val itemsResult = repository.getApprovedItems()

      val error = postsResult.exceptionOrNull()
        ?: pollsResult.exceptionOrNull()
        ?: itemsResult.exceptionOrNull()

      _state.update {
        it.copy(
          loading = false,
          errorMessage = error?.message,
          posts = postsResult.getOrDefault(emptyList()),
          polls = pollsResult.getOrDefault(emptyList()),
          approvedItems = itemsResult.getOrDefault(emptyList())
        )
      }
    }
  }

  fun createPost(userId: String, title: String, body: String) {
    if (title.isBlank() || body.isBlank()) {
      _state.update { it.copy(errorMessage = "제목과 내용을 입력해 주세요.") }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null, actionMessage = null) }
      repository.createPost(userId, title, body)
        .onSuccess {
          _state.update { it.copy(actionMessage = "글이 등록되었습니다.") }
          refresh()
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              errorMessage = "글 등록 실패: ${error.message ?: "알 수 없는 오류"}"
            )
          }
        }
    }
  }

  fun vote(userId: String, pollId: String, optionId: String) {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null, actionMessage = null) }
      repository.votePoll(userId, pollId, optionId)
        .onSuccess {
          _state.update { it.copy(actionMessage = "투표가 반영되었습니다.") }
          refresh()
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              errorMessage = "투표 실패: ${error.message ?: "알 수 없는 오류"}"
            )
          }
        }
    }
  }
}
