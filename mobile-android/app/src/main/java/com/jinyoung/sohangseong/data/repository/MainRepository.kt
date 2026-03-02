package com.jinyoung.sohangseong.data.repository

import com.jinyoung.sohangseong.data.model.ApprovedItemDto
import com.jinyoung.sohangseong.data.model.CommunityPostDto
import com.jinyoung.sohangseong.data.model.VotePollDto
import com.jinyoung.sohangseong.data.network.MainApi

class MainRepository(
  private val mainApi: MainApi
) {
  suspend fun getPosts(): Result<List<CommunityPostDto>> = runCatching { mainApi.getPosts() }

  suspend fun getPolls(): Result<List<VotePollDto>> = runCatching { mainApi.getPolls() }

  suspend fun getApprovedItems(): Result<List<ApprovedItemDto>> = runCatching { mainApi.getApprovedItems() }
}
