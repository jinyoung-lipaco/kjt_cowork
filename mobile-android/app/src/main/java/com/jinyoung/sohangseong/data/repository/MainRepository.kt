package com.jinyoung.sohangseong.data.repository

import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.ApprovedItemDetailDto
import com.jinyoung.sohangseong.data.model.CommunityPostDetailDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.CreateCommentRequest
import com.jinyoung.sohangseong.data.model.CreatePostRequest
import com.jinyoung.sohangseong.data.model.UpdateProfileRequest
import com.jinyoung.sohangseong.data.model.UpdateProfileResponseDto
import com.jinyoung.sohangseong.data.model.UserProfileSummaryDto
import com.jinyoung.sohangseong.data.model.VotePollDetailDto
import com.jinyoung.sohangseong.data.model.VoteRequest
import com.jinyoung.sohangseong.data.model.VotePollDto
import com.jinyoung.sohangseong.data.network.MainApi
import org.json.JSONObject
import retrofit2.HttpException

class MainRepository(
  private val mainApi: MainApi
) {
  suspend fun getPosts(): Result<List<CommunityPostDto>> = runCatching { mainApi.getPosts() }

  suspend fun getPostDetail(postId: String): Result<CommunityPostDetailDto> = runCatching {
    mainApi.getPostDetail(postId)
  }

  suspend fun getPolls(): Result<List<VotePollDto>> = runCatching { mainApi.getPolls() }

  suspend fun getPollDetail(pollId: String): Result<VotePollDetailDto> {
    return runCatching { mainApi.getPollDetail(pollId) }
  }

  suspend fun getApprovedItems(): Result<List<ApprovedItemDto>> = runCatching { mainApi.getApprovedItems() }

  suspend fun getApprovedItemDetail(itemId: String): Result<ApprovedItemDetailDto> = runCatching {
    mainApi.getApprovedItemDetail(itemId)
  }

  suspend fun getProfileSummary(userId: String): Result<UserProfileSummaryDto> {
    return runCatching { mainApi.getProfileSummary(userId) }
  }

  suspend fun updateProfile(
    userId: String,
    nickname: String? = null,
    bio: String? = null,
    interestCategories: List<String>? = null
  ): Result<UpdateProfileResponseDto> {
    return runCatching {
      mainApi.updateProfile(
        userId = userId,
        body = UpdateProfileRequest(
          nickname = nickname?.trim(),
          bio = bio?.trim(),
          interestCategories = interestCategories
        )
      )
    }.recoverCatching { error ->
      throw mapApiException(error, "프로필 변경에 실패했습니다.")
    }
  }

  suspend fun createPost(authorId: String, title: String, body: String): Result<CommunityPostDto> {
    return runCatching {
      mainApi.createPost(
        CreatePostRequest(
          title = title.trim(),
          body = body.trim(),
          authorId = authorId
        )
      )
    }
  }

  suspend fun votePoll(userId: String, pollId: String, optionId: String): Result<Unit> {
    return runCatching {
      mainApi.votePoll(
        pollId = pollId,
        body = VoteRequest(userId = userId, optionId = optionId)
      )
      Unit
    }
  }

  suspend fun createComment(userId: String, postId: String, body: String): Result<Unit> {
    return runCatching {
      mainApi.createComment(
        CreateCommentRequest(
          postId = postId,
          authorId = userId,
          body = body.trim()
        )
      )
      Unit
    }
  }

  private fun mapApiException(error: Throwable, fallback: String): Exception {
    val serverMessage = (error as? HttpException)
      ?.response()
      ?.errorBody()
      ?.string()
      ?.let(::extractErrorMessageField)

    val message = if (serverMessage.isNullOrBlank()) error.message ?: fallback else serverMessage
    return Exception(message, error)
  }

  private fun extractErrorMessageField(raw: String): String? {
    return runCatching {
      JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
    }.getOrNull()
  }
}
