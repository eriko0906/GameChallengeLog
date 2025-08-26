package com.mochichan.gamechallengelog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.RoomListViewModel
import kotlinx.coroutines.launch

@Composable
fun RoomListScreen(
    navController: NavController,
    roomListViewModel: RoomListViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val rooms by roomListViewModel.rooms.collectAsState()
    val user by profileViewModel.user.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("参加中のルーム") },
                actions = {
                    Row(
                        modifier = Modifier
                            .clickable { navController.navigate("profile") }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            text = user?.name ?: "ゲスト",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
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
            items(rooms) { room ->
                RoomCard(
                    gameRoom = room,
                    onClick = { navController.navigate("room_detail/${room.roomId}") }
                )
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
                ListItem(
                    headlineContent = { Text("ルームを作成") },
                    leadingContent = { Icon(Icons.Default.Create, null) },
                    modifier = Modifier.clickable { navController.navigate("create_room") }
                )
                ListItem(
                    headlineContent = { Text("ルームに参加") },
                    leadingContent = { Icon(Icons.Default.GroupAdd, null) },
                    modifier = Modifier.clickable { navController.navigate("join_room") }
                )
            }
        }
    }
}

@Composable
fun RoomCard(gameRoom: GameRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "ルームアイコン",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gameRoom.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "メンバー: ${gameRoom.memberIds.size}人",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "詳細へ",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}