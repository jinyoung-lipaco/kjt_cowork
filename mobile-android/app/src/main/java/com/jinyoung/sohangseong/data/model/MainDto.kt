package com.jinyoung.sohangseong.data.model

data class PostAuthorDto(
  val id: String,
  val nickname: String
)

data class PostCommentDto(
  val id: String,
  val body: String
)

data class PostDetailAuthorDto(
  val id: String,
  val nickname: String,
  val tier: String
)

data class PostDetailCommentDto(
  val id: String,
  val body: String,
  val createdAt: String,
  val author: PostDetailAuthorDto
)

data class CommunityPostDetailDto(
  val id: String,
  val title: String,
  val body: String,
  val createdAt: String,
  val author: PostDetailAuthorDto,
  val comments: List<PostDetailCommentDto>
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

data class VoteDetailOptionDto(
  val id: String,
  val label: String,
  val sortOrder: Int,
  val voteCount: Int
)

data class VotePollDto(
  val id: String,
  val title: String,
  val description: String?,
  val status: String,
  val options: List<VoteOptionDto>,
  val participantCount: Int
)

data class VotePollDetailDto(
  val id: String,
  val title: String,
  val description: String?,
  val status: String,
  val totalVotes: Int,
  val options: List<VoteDetailOptionDto>
)

data class ApprovedItemDto(
  val id: String,
  val category: String,
  val name: String,
  val brand: String?,
  val safetyScore: Int,
  val priceText: String?
)

data class ApprovedItemDetailDto(
  val id: String,
  val category: String,
  val name: String,
  val brand: String?,
  val safetyScore: Int,
  val priceText: String?,
  val criteriaText: String?,
  val status: String,
  val createdAt: String
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

data class CreateCommentRequest(
  val postId: String,
  val authorId: String,
  val body: String
)

data class UserProfileStatsDto(
  val postCount: Int,
  val commentCount: Int,
  val voteCount: Int
)

data class UserRecentPostDto(
  val id: String,
  val title: String,
  val createdAt: String
)

data class UserRecentCommentDto(
  val id: String,
  val body: String,
  val postId: String,
  val createdAt: String
)

data class UserRecentVoteDto(
  val id: String,
  val pollId: String,
  val pollTitle: String,
  val optionLabel: String,
  val createdAt: String
)

data class UserProfileActivityDto(
  val recentPosts: List<UserRecentPostDto>,
  val recentComments: List<UserRecentCommentDto>,
  val recentVotes: List<UserRecentVoteDto>
)

data class UserProfileSummaryDto(
  val id: String,
  val email: String,
  val nickname: String,
  val tier: String,
  val createdAt: String,
  val stats: UserProfileStatsDto,
  val activity: UserProfileActivityDto
)
