@file:OptIn(ExperimentalMaterial3Api::class)

package com.mochichan.gamechallengelog.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.data.MatchHistory
import com.mochichan.gamechallengelog.data.PlayerWithDetails
import com.mochichan.gamechallengelog.data.PenaltyWithPlayer
import com.mochichan.gamechallengelog.data.User
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.RoomDetailViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.mochichan.gamechallengelog.data.MatchHistoryItem

// RoomDetailViewModelFactoryは変更なし

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

    val application = LocalContext.current.applicationContext as Application
    val viewModel: RoomDetailViewModel = viewModel(factory = RoomDetailViewModelFactory(application, roomId))
    val room by viewModel.room.collectAsState()
    val playersWithDetails by viewModel.playersWithDetails.collectAsState()
    val pendingPenalties by viewModel.pendingPenalties.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()
    val user by profileViewModel.user.collectAsState()

    if (room == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        RoomDetailContent(
            navController = navController,
            room = room!!,
            players = playersWithDetails,
            penalties = pendingPenalties,
            history = matchHistory,
            user = user,
            viewModel = viewModel
        )
    }
}


@Composable
fun RoomDetailContent(
    navController: NavController,
    room: GameRoom,
    players: List<PlayerWithDetails>,
    penalties: List<PenaltyWithPlayer>,
    history: List<MatchHistoryItem>,
    user: User?,
    viewModel: RoomDetailViewModel
) {
    var buttonsEnabled by remember { mutableStateOf(true) }
    var showAddGuestDialog by remember { mutableStateOf(false) }
    var newGuestName by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showLeaveRoomDialog by remember { mutableStateOf(false) }
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
                                text = { Text("ルームを退会する", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showLeaveRoomDialog = true
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                })
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = {
                        if (buttonsEnabled) {
                            buttonsEnabled = false
                            navController.navigate("game_management/${room.roomId}")
                        }
                    }, modifier = Modifier.weight(1f)) { Text("ゲーム管理") }
                    Button(onClick = {
                        if (buttonsEnabled) {
                            buttonsEnabled = false
                            navController.navigate("player_stats/${room.roomId}")
                        }
                    }, modifier = Modifier.weight(1f)) { Text("成績を見る") }
                }
                Button(onClick = {
                    if (buttonsEnabled) {
                        buttonsEnabled = false
                        navController.navigate("record_match/${room.roomId}")
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("新しい対戦を記録する")
                }
            }
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
                    EmptyState(
                        icon = Icons.Default.People,
                        message = "まだプレイヤーがいません。\n「ゲストを追加」から招待しましょう！"
                    )
                }
            } else {
                items(players) { playerWithDetails ->
                    // --- ↓↓↓ ここのUIを、新しいデータ構造に合わせて修正 ↓↓↓ ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!playerWithDetails.user?.iconUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = playerWithDetails.user?.iconUrl,
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
                            text = if (playerWithDetails.user != null) {
                                playerWithDetails.user.name // アプリユーザーなら最新のユーザー名
                            } else {
                                "${playerWithDetails.player.guestName} (ゲスト)" // ゲストならゲスト名
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("やることリスト", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (penalties.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        message = "未完了のペナルティはありません。"
                    )
                }
            } else {
                items(penalties) { penaltyWithPlayer ->
                    // --- ↓↓↓ ここのUIを、新しいデータ構造に合わせて修正 ↓↓↓ ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = {
                                viewModel.completePenalty(penaltyWithPlayer.penalty)
                            }
                        )
                        Text(
                            text = "${penaltyWithPlayer.penalty.description} (担当: ${
                                if (penaltyWithPlayer.playerWithDetails.user != null) {
                                    penaltyWithPlayer.playerWithDetails.user.name // アプリユーザーなら最新のユーザー名
                                } else {
                                    penaltyWithPlayer.playerWithDetails.player.guestName // ゲストならゲスト名
                                }
                            })"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("最近の記録", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (history.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        message = "まだ対戦記録がありません。"
                    )
                }
            } else {
                // --- ↓↓↓ ここの表示ロジックを、より高度なものに書き換えます ↓↓↓ ---
                items(history) { historyItem ->
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val dateString = dateFormat.format(historyItem.match.matchDate)

                    // 勝者の名前をリストアップする
                    val winnerNames = historyItem.results
                        .filter { it.result.outcome == "win" }
                        .joinToString(", ") { resultWithPlayer ->
                            if (resultWithPlayer.playerWithDetails.user != null) {
                                resultWithPlayer.playerWithDetails.user.name // アプリユーザーなら最新の名前
                            } else {
                                resultWithPlayer.playerWithDetails.player.guestName ?: "" // ゲストならゲスト名
                            }
                        }

                    Text("$dateString ${historyItem.game.name} (勝者: ${if(winnerNames.isBlank()) "なし" else winnerNames})")
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
                        viewModel.addGuestPlayer(room.roomId, newGuestName)
                        newGuestName = ""
                        showAddGuestDialog = false
                    }) { Text("追加") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGuestDialog = false }) { Text("キャンセル") }
                }
            )
        }

        if (showLeaveRoomDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveRoomDialog = false },
                title = { Text("ルームを退会") },
                text = { Text("本当にこのルームを退会しますか？\n（ゲストとして追加した情報などは残ります）") },
                confirmButton = {
                    Button(
                        onClick = {
                            user?.let {
                                viewModel.leaveRoom(it.userId)
                            }
                            showLeaveRoomDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("退会する")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveRoomDialog = false }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
class RoomDetailViewModelFactory(
    private val application: Application,
    private val roomId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomDetailViewModel(application, roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}