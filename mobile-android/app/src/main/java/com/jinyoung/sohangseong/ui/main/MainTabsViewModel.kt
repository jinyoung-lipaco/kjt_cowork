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
}
