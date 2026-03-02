package com.jinyoung.sohangseong.data.repository

import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.CreateCommentRequest
import com.jinyoung.sohangseong.data.model.CreatePostRequest
import com.jinyoung.sohangseong.data.model.UserProfileSummaryDto
import com.jinyoung.sohangseong.data.model.VoteRequest
import com.jinyoung.sohangseong.data.model.VotePollDto
import com.jinyoung.sohangseong.data.network.MainApi

class MainRepository(
  private val mainApi: MainApi
) {
  suspend fun getPosts(): Result<List<CommunityPostDto>> = runCatching { mainApi.getPosts() }

  suspend fun getPolls(): Result<List<VotePollDto>> = runCatching { mainApi.getPolls() }

  suspend fun getApprovedItems(): Result<List<ApprovedItemDto>> = runCatching { mainApi.getApprovedItems() }

  suspend fun getProfileSummary(userId: String): Result<UserProfileSummaryDto> {
    return runCatching { mainApi.getProfileSummary(userId) }
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
}
