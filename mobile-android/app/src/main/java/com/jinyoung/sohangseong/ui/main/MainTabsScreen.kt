package com.jinyoung.sohangseong.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainTabsScreen(
  state: MainTabsUiState,
  userId: String,
  nickname: String,
  onSelectTab: (MainTab) -> Unit,
  onRefresh: () -> Unit,
  onCreatePost: (title: String, body: String) -> Unit,
  onCreateComment: (postId: String, body: String) -> Unit,
  onVote: (pollId: String, optionId: String) -> Unit,
  onSignOut: () -> Unit
) {
  Scaffold(
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = state.selectedTab == MainTab.COMMUNITY,
          onClick = { onSelectTab(MainTab.COMMUNITY) },
          label = { Text("커뮤니티") },
          icon = { Text("💬") }
        )
        NavigationBarItem(
          selected = state.selectedTab == MainTab.STANDARDS,
          onClick = { onSelectTab(MainTab.STANDARDS) },
          label = { Text("스탠다드") },
          icon = { Text("🛡️") }
        )
        NavigationBarItem(
          selected = state.selectedTab == MainTab.PROFILE,
          onClick = { onSelectTab(MainTab.PROFILE) },
          label = { Text("프로필") },
          icon = { Text("👤") }
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      if (state.errorMessage != null) {
        Text(
          text = "데이터 로드 실패: ${state.errorMessage}",
          color = MaterialTheme.colorScheme.error
        )
      }
      if (state.actionMessage != null) {
        Text(
          text = state.actionMessage,
          color = MaterialTheme.colorScheme.primary
        )
      }
      if (state.loading) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(top = 2.dp))
          Text("데이터를 불러오는 중입니다.", style = MaterialTheme.typography.bodySmall)
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onRefresh) { Text("새로고침") }
        Button(onClick = onSignOut) { Text("로그아웃") }
      }

      when (state.selectedTab) {
        MainTab.COMMUNITY -> CommunityTab(
          state = state,
          onCreatePost = { title, body -> onCreatePost(title, body) },
          onCreateComment = onCreateComment
        )
        MainTab.STANDARDS -> StandardsTab(
          state = state,
          onVote = onVote
        )
        MainTab.PROFILE -> ProfileTab(userId = userId, nickname = nickname, state = state)
      }
    }
  }
}

@Composable
private fun CommunityTab(
  state: MainTabsUiState,
  onCreatePost: (title: String, body: String) -> Unit,
  onCreateComment: (postId: String, body: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var body by remember { mutableStateOf("") }
  val commentInputs = remember { mutableStateMapOf<String, String>() }

  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("새 글 작성", style = MaterialTheme.typography.titleMedium)
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("제목") },
            singleLine = true
          )
          OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("내용") }
          )
          Button(
            onClick = {
              onCreatePost(title, body)
              title = ""
              body = ""
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("등록")
          }
        }
      }
    }

    if (state.posts.isEmpty() && !state.loading) {
      item {
        EmptyStateCard("아직 게시글이 없습니다. 첫 글을 작성해 보세요.")
      }
    }

    items(state.posts) { post ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = post.title, style = MaterialTheme.typography.titleMedium)
          Text(text = post.body, style = MaterialTheme.typography.bodyMedium)
          Text(
            text = "작성자 ${post.author.nickname} · 댓글 ${post.comments.size}개",
            style = MaterialTheme.typography.bodySmall
          )
          if (post.comments.isEmpty()) {
            Text("- 아직 댓글이 없습니다.", style = MaterialTheme.typography.bodySmall)
          } else {
            post.comments.forEach { comment ->
              Text(
                text = "• ${comment.body}",
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
          OutlinedTextField(
            value = commentInputs[post.id] ?: "",
            onValueChange = { commentInputs[post.id] = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("댓글 작성") }
          )
          Button(
            onClick = {
              onCreateComment(post.id, commentInputs[post.id] ?: "")
              commentInputs[post.id] = ""
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("댓글 등록")
          }
        }
      }
    }
  }
}

@Composable
private fun StandardsTab(
  state: MainTabsUiState,
  onVote: (pollId: String, optionId: String) -> Unit
) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text("진행 중 투표", style = MaterialTheme.typography.titleMedium)
    }
    if (state.polls.isEmpty() && !state.loading) {
      item {
        EmptyStateCard("현재 진행 중인 투표가 없습니다.")
      }
    }
    items(state.polls) { poll ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = poll.title, style = MaterialTheme.typography.titleSmall)
          Text(
            text = "${poll.status} · 참여 ${poll.participantCount}명 · 옵션 ${poll.options.size}개",
            style = MaterialTheme.typography.bodySmall
          )
          if (!poll.description.isNullOrBlank()) {
            Text(text = poll.description, style = MaterialTheme.typography.bodyMedium)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            poll.options.forEach { option ->
              Button(
                onClick = { onVote(poll.id, option.id) },
                enabled = !state.loading,
                modifier = Modifier.weight(1f)
              ) {
                Text(option.label)
              }
            }
          }
        }
      }
    }

    item {
      Text("인정템", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
    }
    if (state.approvedItems.isEmpty() && !state.loading) {
      item {
        EmptyStateCard("등록된 인정템이 없습니다.")
      }
    }
    items(state.approvedItems) { item ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = item.name, style = MaterialTheme.typography.titleSmall)
          Text(
            text = "${item.category} · 안전점수 ${item.safetyScore}",
            style = MaterialTheme.typography.bodySmall
          )
          Text(
            text = "${item.brand ?: "브랜드 미표기"} · ${item.priceText ?: "가격 정보 없음"}",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileTab(userId: String, nickname: String, state: MainTabsUiState) {
  val summary = state.profileSummary
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("프로필", style = MaterialTheme.typography.titleMedium)

      if (summary == null && state.loading) {
        Text("프로필 정보를 불러오는 중입니다.", style = MaterialTheme.typography.bodySmall)
        return@Column
      }
      if (summary == null && !state.loading) {
        Text("프로필 정보를 불러오지 못했습니다. 새로고침을 눌러 다시 시도해 주세요.", style = MaterialTheme.typography.bodySmall)
        return@Column
      }

      Text("사용자 ID: ${summary?.id ?: userId}")
      Text("닉네임: ${summary?.nickname ?: nickname}")
      Text("이메일: ${summary?.email ?: "-"}")
      Text("등급: ${summary?.tier ?: "-"}")
      Text("가입일: ${summary?.createdAt?.take(10) ?: "-"}")
      Text("커뮤니티 글: ${summary?.stats?.postCount ?: state.posts.size}개")
      Text("작성 댓글: ${summary?.stats?.commentCount ?: 0}개")
      Text("참여 투표: ${summary?.stats?.voteCount ?: 0}개")
      Text("인정템: ${state.approvedItems.size}개")

      Text("최근 작성 글", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
      val recentPosts = summary?.activity?.recentPosts.orEmpty()
      if (recentPosts.isEmpty()) {
        Text("- 최근 글이 없습니다.", style = MaterialTheme.typography.bodySmall)
      } else {
        recentPosts.forEach { post ->
          Text(
            "• ${post.title} (${post.createdAt.take(10)})",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }

      Text("최근 댓글", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
      val recentComments = summary?.activity?.recentComments.orEmpty()
      if (recentComments.isEmpty()) {
        Text("- 최근 댓글이 없습니다.", style = MaterialTheme.typography.bodySmall)
      } else {
        recentComments.forEach { comment ->
          Text(
            "• ${comment.body} (${comment.createdAt.take(10)})",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }

      Text("최근 투표", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
      val recentVotes = summary?.activity?.recentVotes.orEmpty()
      if (recentVotes.isEmpty()) {
        Text("- 최근 투표 이력이 없습니다.", style = MaterialTheme.typography.bodySmall)
      } else {
        recentVotes.forEach { vote ->
          Text(
            "• ${vote.pollTitle} / ${vote.optionLabel} (${vote.createdAt.take(10)})",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyStateCard(message: String) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = message,
      modifier = Modifier.padding(12.dp),
      style = MaterialTheme.typography.bodySmall
    )
  }
}
