package com.mochichan.gamechallengelog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mochichan.gamechallengelog.data.GameRoomWithPenaltyCount // ← これを追加
import com.mochichan.gamechallengelog.ui.viewmodels.RoomListViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape // ← これを追加
import androidx.compose.ui.draw.clip // ← これを追加
import androidx.compose.ui.layout.ContentScale // ← これを追加
import coil.compose.AsyncImage // ← これを追加

@Composable
fun RoomListScreen(
    navController: NavController,
    roomListViewModel: RoomListViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel() // ← ViewModelを受け取る
) {
    // --- ↓↓↓ 受け取るデータの型を変更 ↓↓↓ ---
    val roomsWithPenaltyCount by roomListViewModel.roomsWithPenaltyCount.collectAsState()
    val user by profileViewModel.user.collectAsState() // ← ユーザー情報を取得
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val userName = user?.name ?: "ゲスト"

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("参加中のルーム") },
                actions = {
                    // ↓↓↓ プロフィール表示部分を修正 ↓↓↓
                    Row(
                        modifier = Modifier
                            .clickable { navController.navigate("profile") }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- ↓↓↓ アイコン表示部分を修正 ↓↓↓ ---
                        if (!user?.iconUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.iconUrl,
                                contentDescription = "プロフィール画像",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "プロフィール")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userName,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "ルームを追加")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- ↓↓↓ itemsに渡すリストと、RoomCardに渡すデータを変更 ↓↓↓ ---
            items(roomsWithPenaltyCount) { roomWithCount ->
                RoomCard(
                    roomWithCount = roomWithCount, // 新しいデータを渡す
                    onClick = { navController.navigate("room_detail/${roomWithCount.gameRoom.roomId}") }
                )
            }
        }

        if (showBottomSheet) {
            // ... (BottomSheetの中身は変更なし) ...
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
                ListItem(
                    headlineContent = { Text("ルームを作成") },
                    leadingContent = { Icon(Icons.Default.Create, null) },
                    modifier = Modifier.clickable {
                        navController.navigate("create_room")
                    }
                )
                ListItem(
                    headlineContent = { Text("ルームに参加") },
                    leadingContent = { Icon(Icons.Default.PersonAdd, null) },
                    modifier = Modifier.clickable {
                        navController.navigate("join_room")
                    }
                )
            }
        }
    }
}

// --- ↓↓↓ RoomCardが受け取るデータの型を変更 ↓↓↓ ---
@Composable
fun RoomCard(roomWithCount: GameRoomWithPenaltyCount, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = roomWithCount.gameRoom.name, // ルーム名
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                // データベースから取得したペナルティ件数を表示
                text = "未完了のペナルティ: ${roomWithCount.penaltyCount} 件 ${if (roomWithCount.penaltyCount > 0) "🔥" else "✨"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (roomWithCount.penaltyCount > 0) MaterialTheme.colorScheme.error else LocalContentColor.current
            )
        }
    }
}