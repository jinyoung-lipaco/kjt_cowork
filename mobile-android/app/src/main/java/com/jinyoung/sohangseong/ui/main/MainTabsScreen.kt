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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainTabsScreen(
  state: MainTabsUiState,
  nickname: String,
  onSelectTab: (MainTab) -> Unit,
  onRefresh: () -> Unit,
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

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onRefresh) { Text("새로고침") }
        Button(onClick = onSignOut) { Text("로그아웃") }
      }

      when (state.selectedTab) {
        MainTab.COMMUNITY -> CommunityTab(state = state)
        MainTab.STANDARDS -> StandardsTab(state = state)
        MainTab.PROFILE -> ProfileTab(nickname = nickname, state = state)
      }
    }
  }
}

@Composable
private fun CommunityTab(state: MainTabsUiState) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    items(state.posts) { post ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = post.title, style = MaterialTheme.typography.titleMedium)
          Text(text = post.body, style = MaterialTheme.typography.bodyMedium)
          Text(
            text = "작성자 ${post.author.nickname} · 댓글 ${post.comments.size}개",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}

@Composable
private fun StandardsTab(state: MainTabsUiState) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text("진행 중 투표", style = MaterialTheme.typography.titleMedium)
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
        }
      }
    }

    item {
      Text("인정템", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
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
private fun ProfileTab(nickname: String, state: MainTabsUiState) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("프로필", style = MaterialTheme.typography.titleMedium)
      Text("닉네임: $nickname")
      Text("커뮤니티 글: ${state.posts.size}개")
      Text("진행/종료 투표: ${state.polls.size}개")
      Text("인정템: ${state.approvedItems.size}개")
    }
  }
}
