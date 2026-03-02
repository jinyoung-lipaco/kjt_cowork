package com.jinyoung.sohangseong.data.model

data class PostAuthorDto(
  val id: String,
  val nickname: String
)

data class PostCommentDto(
  val id: String,
  val body: String
)

data class CommunityPostDto(
  val id: String,
  val title: String,
  val body: String,
  val author: PostAuthorDto,
  val comments: List<PostCommentDto>
)

data class VoteOptionDto(
  val id: String,
  val label: String,
  val sortOrder: Int
)

data class VotePollDto(
  val id: String,
  val title: String,
  val description: String?,
  val status: String,
  val options: List<VoteOptionDto>,
  val participantCount: Int
)

data class ApprovedItemDto(
  val id: String,
  val category: String,
  val name: String,
  val brand: String?,
  val safetyScore: Int,
  val priceText: String?
)

data class CreatePostRequest(
  val title: String,
  val body: String,
  val authorId: String
)

data class VoteRequest(
  val userId: String,
  val optionId: String
)
