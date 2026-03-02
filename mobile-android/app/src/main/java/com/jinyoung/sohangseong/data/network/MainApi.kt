package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.CreateCommentRequest
import com.jinyoung.sohangseong.data.model.CreatePostRequest
import com.jinyoung.sohangseong.data.model.UserProfileSummaryDto
import com.jinyoung.sohangseong.data.model.VoteRequest
import com.jinyoung.sohangseong.data.model.VotePollDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MainApi {
  @GET("posts")
  suspend fun getPosts(): List<CommunityPostDto>

  @GET("standards/polls")
  suspend fun getPolls(): List<VotePollDto>

  @GET("standards/approved-items")
  suspend fun getApprovedItems(): List<ApprovedItemDto>

  @GET("users/{userId}/profile-summary")
  suspend fun getProfileSummary(
    @Path("userId") userId: String
  ): UserProfileSummaryDto

  @POST("posts")
  suspend fun createPost(
    @Body body: CreatePostRequest
  ): CommunityPostDto

  @POST("posts/comments")
  suspend fun createComment(
    @Body body: CreateCommentRequest
  ): Map<String, String>

  @POST("standards/polls/{pollId}/vote")
  suspend fun votePoll(
    @Path("pollId") pollId: String,
    @Body body: VoteRequest
  ): Map<String, Boolean>
}
