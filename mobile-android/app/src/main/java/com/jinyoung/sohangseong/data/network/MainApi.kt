package com.jinyoung.sohangseong.data.network

import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.VotePollDto
import retrofit2.http.GET

interface MainApi {
  @GET("posts")
  suspend fun getPosts(): List<CommunityPostDto>

  @GET("standards/polls")
  suspend fun getPolls(): List<VotePollDto>

  @GET("standards/approved-items")
  suspend fun getApprovedItems(): List<ApprovedItemDto>
}
