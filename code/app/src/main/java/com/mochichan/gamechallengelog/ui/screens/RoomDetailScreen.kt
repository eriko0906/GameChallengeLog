@file:OptIn(ExperimentalMaterial3Api::class)

package com.mochichan.gamechallengelog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.data.Player
import com.mochichan.gamechallengelog.data.User
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.RoomDetailViewModel
import kotlinx.coroutines.launch

// RoomDetailViewModelにroomIdを渡すための、新しい専用の「工場」
class RoomDetailViewModelFactory(private val roomId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomDetailViewModel(roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Composable
fun RoomDetailScreen(
    navController: NavController,
    roomId: String?,
    profileViewModel: ProfileViewModel = viewModel()
) {
    if (roomId == null) {
        Text("エラー：ルームIDが見つかりません。")
        return
    }

    // 新しい工場を使ってViewModelを作成
    val viewModel: RoomDetailViewModel = viewModel(factory = RoomDetailViewModelFactory(roomId))
    val room by viewModel.room.collectAsState()
    val players by viewModel.players.collectAsState()
    val user by profileViewModel.user.collectAsState()

    if (room == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        RoomDetailContent(
            navController = navController,
            room = room!!,
            players = players,
            user = user,
            viewModel = viewModel
        )
    }
}


@Composable
fun RoomDetailContent(
    navController: NavController,
    room: GameRoom,
    players: List<Player>,
    user: User?,
    viewModel: RoomDetailViewModel
) {
    var buttonsEnabled by remember { mutableStateOf(true) }
    var showAddGuestDialog by remember { mutableStateOf(false) }
    var newGuestName by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(room.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (buttonsEnabled) {
                            buttonsEnabled = false
                            navController.popBackStack()
                        }
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る") }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clickable { navController.navigate("profile") }
                            .padding(horizontal = 8.dp),
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
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "メニュー")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("ルームを退会する（未実装）", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    // TODO: 退会処理
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                })
        },
        bottomBar = {
            // ... (下部のアクションボタンは、一旦コメントアウトします) ...
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ルームID", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(room.roomId))
                        scope.launch {
                            snackbarHostState.showSnackbar("ルームIDをコピーしました！")
                        }
                    }) {
                        Text(room.roomId)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ContentCopy, contentDescription = "コピー", modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("プレイヤーリスト", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { showAddGuestDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ゲストを追加")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (players.isEmpty()) {
                item {
                    Text("まだプレイヤーがいません。")
                }
            } else {
                items(players) { player ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!player.iconUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = player.iconUrl,
                                contentDescription = "プレイヤーアイコン",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "デフォルトアイコン",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (player.isGuest) "${player.guestName} (ゲスト)" else player.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (showAddGuestDialog) {
            AlertDialog(
                onDismissRequest = { showAddGuestDialog = false },
                title = { Text("ゲストプレイヤーを追加") },
                text = {
                    OutlinedTextField(
                        value = newGuestName,
                        onValueChange = { newGuestName = it },
                        label = { Text("プレイヤー名") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addGuestPlayer(newGuestName)
                        newGuestName = ""
                        showAddGuestDialog = false
                    }) { Text("追加") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGuestDialog = false }) { Text("キャンセル") }
                }
            )
        }
    }
}