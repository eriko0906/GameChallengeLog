@file:OptIn(ExperimentalMaterial3Api::class)

package com.mochichan.gamechallengelog.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // ← importを追加
import androidx.compose.foundation.lazy.items    // ← importを追加
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.data.MatchHistory
import com.mochichan.gamechallengelog.data.PlayerWithDetails
import com.mochichan.gamechallengelog.data.PenaltyWithPlayer
import com.mochichan.gamechallengelog.ui.viewmodels.RoomDetailViewModel
import com.mochichan.gamechallengelog.data.User
import java.text.SimpleDateFormat // ← importを追加
import java.util.Locale         // ← importを追加
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape // ← これを追加
import androidx.compose.ui.draw.clip // ← これを追加
import androidx.compose.ui.layout.ContentScale // ← これを追加
import coil.compose.AsyncImage // ← これを追加
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch

// ViewModelを生成するための「工場」（変更なし）
class RoomDetailViewModelFactory(private val application: Application, private val roomId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomDetailViewModel(application, roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun RoomDetailScreen(navController: NavController, roomId: String?, profileViewModel: ProfileViewModel = viewModel()    ) {
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
    val userName = user?.name ?: "ゲスト"

    if (room == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // --- ↓↓↓ ここでplayersとviewModelを渡すように修正 ↓↓↓ ---
        RoomDetailContent(
            navController = navController,
            room = room!!,
            players = playersWithDetails, // ← 新しいデータを渡す
            penalties = pendingPenalties, // ← この行を追加
            history = matchHistory,
            userName = user?.name ?: "ゲスト",
            user = user,
            viewModel = viewModel
        )
    }
}

@Composable
fun RoomDetailContent(
    navController: NavController,
    room: GameRoom,
    players: List<PlayerWithDetails>, // ← 受け取るデータの型を変更
    penalties: List<PenaltyWithPlayer>,
    history: List<MatchHistory>,
    userName: String,
    user: User?,
    viewModel: RoomDetailViewModel
) {
    var buttonsEnabled by remember { mutableStateOf(true) }
    var showAddGuestDialog by remember { mutableStateOf(false) }
    var newGuestName by remember { mutableStateOf("") }
    // --- ↓↓↓ 新しいUIの状態管理変数を追加 ↓↓↓ ---
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
                    }) { Icon(Icons.Default.ArrowBack, "戻る") }
                }, actions = {
                    // --- ↓↓↓ ここから修正 ↓↓↓ ---
                    // プロフィール表示とメニューボタンを、さらにRowで囲む
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // プロフィール表示部分（変更なし）
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
                        // メニューボタン部分（変更なし）
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
                    }
                    // --- ↑↑↑ ここまで修正 ---
                })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
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
            if (players.isEmpty()) {
                Text("まだプレイヤーがいません。")
            } else {
                // --- ↓↓↓ プレイヤーリストの表示UIをリッチなものに修正 ↓↓↓ ---
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(players) { playerWithDetails ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // アイコン表示
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
                            // 名前表示 (ユーザーかゲストかで表示を分ける)
                            Text(
                                text = if (playerWithDetails.user != null) {
                                    playerWithDetails.user.name
                                } else {
                                    "${playerWithDetails.player.guestName} (ゲスト)"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("やることリスト", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (penalties.isEmpty()) {
                Text("未完了のペナルティはありません。")
            } else {
                LazyColumn {
                    items(penalties) { penaltyWithPlayer ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = false, // 未完了なので常にfalse
                                onCheckedChange = {
                                    // チェックされたら、ViewModelに完了を通知
                                    viewModel.completePenalty(penaltyWithPlayer.penalty)
                                }
                            )
                            Text(
                                text = "${penaltyWithPlayer.penalty.description} (担当: ${penaltyWithPlayer.player.guestName ?: "アプリユーザー"})"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("最近の記録", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text("まだ対戦記録がありません。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(history) { match ->
                        // 日付を「MM/dd」形式にフォーマット
                        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                        val dateString = dateFormat.format(match.matchDate)
                        Text("$dateString ${match.gameName} (勝者: ${match.winnerName ?: "複数"})")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
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
            Spacer(modifier = Modifier.height(8.dp))
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
        } // --- ← Columnの閉じカッコがここに必要でした

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
                text = { Text("本当にこのルームを退会しますか？\n（最後のユーザ（ゲストユーザを除く）の場合ルームは削除されます）") },
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