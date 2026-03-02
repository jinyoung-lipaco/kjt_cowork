package com.jinyoung.sohangseong.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.ApprovedItemDetailDto
import com.jinyoung.sohangseong.data.model.CommunityPostDetailDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.UserProfileSummaryDto
import com.jinyoung.sohangseong.data.model.VotePollDetailDto
import com.jinyoung.sohangseong.data.model.VotePollDto
import com.jinyoung.sohangseong.data.repository.MainRepository
import java.io.IOException
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
  val selectedPostId: String? = null,
  val selectedPollId: String? = null,
  val selectedApprovedItemId: String? = null,
  val isOffline: Boolean = false,
  val loading: Boolean = false,
  val errorMessage: String? = null,
  val actionMessage: String? = null,
  val posts: List<CommunityPostDto> = emptyList(),
  val polls: List<VotePollDto> = emptyList(),
  val selectedPostDetail: CommunityPostDetailDto? = null,
  val selectedPollDetail: VotePollDetailDto? = null,
  val approvedItems: List<ApprovedItemDto> = emptyList(),
  val selectedApprovedItemDetail: ApprovedItemDetailDto? = null,
  val profileSummary: UserProfileSummaryDto? = null
)

class MainTabsViewModel(
  private val repository: MainRepository
) : ViewModel() {
  private val _state = MutableStateFlow(MainTabsUiState())
  val state: StateFlow<MainTabsUiState> = _state.asStateFlow()

  fun selectTab(tab: MainTab) {
    _state.update {
      it.copy(
        selectedTab = tab,
        selectedPostId = null,
        selectedPollId = null,
        selectedApprovedItemId = null,
        selectedPostDetail = null,
        selectedPollDetail = null,
        selectedApprovedItemDetail = null
      )
    }
  }

  fun openPostDetail(postId: String) {
    _state.update {
      it.copy(
        selectedTab = MainTab.COMMUNITY,
        selectedPostId = postId,
        selectedPostDetail = null
      )
    }
    loadPostDetail(postId)
  }

  fun closePostDetail() {
    _state.update { it.copy(selectedPostId = null, selectedPostDetail = null) }
  }

  fun openPollDetail(pollId: String) {
    _state.update {
      it.copy(
        selectedTab = MainTab.STANDARDS,
        selectedPollId = pollId,
        selectedApprovedItemId = null,
        selectedPollDetail = null
      )
    }
    loadPollDetail(pollId)
  }

  fun openApprovedItemDetail(itemId: String) {
    _state.update {
      it.copy(
        selectedTab = MainTab.STANDARDS,
        selectedApprovedItemId = itemId,
        selectedPollId = null,
        selectedApprovedItemDetail = null
      )
    }
    loadApprovedItemDetail(itemId)
  }

  fun closeStandardsDetail() {
    _state.update {
      it.copy(
        selectedPollId = null,
        selectedApprovedItemId = null,
        selectedPollDetail = null,
        selectedApprovedItemDetail = null
      )
    }
  }

  fun refresh(userId: String) {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null, isOffline = false) }

      val postsResult = repository.getPosts()
      val pollsResult = repository.getPolls()
      val itemsResult = repository.getApprovedItems()
      val profileResult = repository.getProfileSummary(userId)

      val error = postsResult.exceptionOrNull()
        ?: pollsResult.exceptionOrNull()
        ?: itemsResult.exceptionOrNull()
        ?: profileResult.exceptionOrNull()

      _state.update {
        it.copy(
          loading = false,
          errorMessage = toUiErrorMessage(error, "데이터를 불러오지 못했습니다."),
          isOffline = isOfflineError(error),
          posts = postsResult.getOrDefault(emptyList()),
          polls = pollsResult.getOrDefault(emptyList()),
          approvedItems = itemsResult.getOrDefault(emptyList()),
          profileSummary = profileResult.getOrNull()
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
      _state.update { it.copy(loading = true, errorMessage = null, actionMessage = null, isOffline = false) }
      repository.createPost(userId, title, body)
        .onSuccess {
          _state.update { it.copy(actionMessage = "글이 등록되었습니다.") }
          refresh(userId)
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              errorMessage = "글 등록 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  fun vote(userId: String, pollId: String, optionId: String) {
    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null, actionMessage = null, isOffline = false) }
      repository.votePoll(userId, pollId, optionId)
        .onSuccess {
          _state.update { it.copy(actionMessage = "투표가 반영되었습니다.") }
          refresh(userId)
          loadPollDetail(pollId)
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              errorMessage = "투표 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  fun createComment(userId: String, postId: String, body: String) {
    if (body.isBlank()) {
      _state.update { it.copy(errorMessage = "댓글 내용을 입력해 주세요.") }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null, actionMessage = null, isOffline = false) }
      repository.createComment(userId = userId, postId = postId, body = body)
        .onSuccess {
          _state.update { it.copy(actionMessage = "댓글이 등록되었습니다.") }
          refresh(userId)
          loadPostDetail(postId)
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              errorMessage = "댓글 등록 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  fun updateProfileNickname(userId: String, nickname: String) {
    val trimmed = nickname.trim()
    if (trimmed.length < 2) {
      _state.update { it.copy(errorMessage = "닉네임은 2자 이상 입력해 주세요.") }
      return
    }
    if (trimmed.length > 20) {
      _state.update { it.copy(errorMessage = "닉네임은 20자 이하로 입력해 주세요.") }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(loading = true, errorMessage = null, actionMessage = null, isOffline = false) }
      repository.updateProfileNickname(userId = userId, nickname = trimmed)
        .onSuccess {
          _state.update { it.copy(actionMessage = "닉네임이 변경되었습니다.") }
          refresh(userId)
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              loading = false,
              errorMessage = "프로필 수정 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  private fun loadPollDetail(pollId: String) {
    viewModelScope.launch {
      repository.getPollDetail(pollId)
        .onSuccess { detail ->
          _state.update { it.copy(selectedPollDetail = detail, errorMessage = null, isOffline = false) }
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              errorMessage = "투표 상세 조회 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  private fun loadPostDetail(postId: String) {
    viewModelScope.launch {
      repository.getPostDetail(postId)
        .onSuccess { detail ->
          _state.update { it.copy(selectedPostDetail = detail, errorMessage = null, isOffline = false) }
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              errorMessage = "게시글 상세 조회 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  private fun loadApprovedItemDetail(itemId: String) {
    viewModelScope.launch {
      repository.getApprovedItemDetail(itemId)
        .onSuccess { detail ->
          _state.update { it.copy(selectedApprovedItemDetail = detail, errorMessage = null, isOffline = false) }
        }
        .onFailure { error ->
          _state.update {
            it.copy(
              errorMessage = "인정템 상세 조회 실패: ${toUiErrorMessage(error, "알 수 없는 오류")}",
              isOffline = isOfflineError(error)
            )
          }
        }
    }
  }

  fun consumeErrorMessage() {
    _state.update { it.copy(errorMessage = null) }
  }

  fun consumeActionMessage() {
    _state.update { it.copy(actionMessage = null) }
  }

  private fun isOfflineError(error: Throwable?): Boolean {
    var current = error
    while (current != null) {
      if (current is IOException) return true
      current = current.cause
    }
    return false
  }

  private fun toUiErrorMessage(error: Throwable?, fallback: String): String {
    return if (isOfflineError(error)) {
      "오프라인 상태입니다. 네트워크 연결 후 다시 시도해 주세요."
    } else {
      error?.message ?: fallback
    }
  }
}
