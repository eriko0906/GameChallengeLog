@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.mochichan.gamechallengelog.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.mochichan.gamechallengelog.data.Game
import com.mochichan.gamechallengelog.data.Player
import com.mochichan.gamechallengelog.ui.viewmodels.RecordMatchViewModel

// ViewModelを生成するための「工場」（変更なし）
class RecordMatchViewModelFactory(private val application: Application, private val roomId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordMatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordMatchViewModel(application, roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun RecordMatchScreen(navController: NavController, roomId: String?) {
    if (roomId == null) {
        Text("エラー：ルームIDが見つかりません。")
        return
    }

    val application = LocalContext.current.applicationContext as Application
    val viewModel: RecordMatchViewModel = viewModel(factory = RecordMatchViewModelFactory(application, roomId))
    val players by viewModel.players.collectAsState()
    val games by viewModel.games.collectAsState()

    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var selectedWinners by remember { mutableStateOf<Set<Player>>(emptySet()) }
    var selectedLosers by remember { mutableStateOf<Set<Player>>(emptySet()) }
    var penaltyDescription by remember { mutableStateOf("") }
    var buttonsEnabled by remember { mutableStateOf(true) }

    // --- ↓↓↓ 新しいUIの状態管理変数を追加 ↓↓↓ ---
    var gameMenuExpanded by remember { mutableStateOf(false) }
    var showAddGameDialog by remember { mutableStateOf(false) }
    var newGameName by remember { mutableStateOf("") }


    Scaffold(topBar = {
        TopAppBar(title = { Text("新しい対戦の記録") }, navigationIcon = {
            IconButton(onClick = {
                if (buttonsEnabled) {
                    buttonsEnabled = false
                    navController.popBackStack()
                }
            }) { Icon(Icons.Default.ArrowBack, "戻る") }
        })
    }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // 間隔を広げる
        ) {
            // --- ゲーム選択 ---
            item {
                Text("ゲーム", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // --- ↓↓↓ ゲーム選択UIをBoxとDropdownMenuで実装 ↓↓↓ ---
                ExposedDropdownMenuBox(
                    expanded = gameMenuExpanded,
                    onExpandedChange = { gameMenuExpanded = !gameMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGame?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ゲームを選択") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gameMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = gameMenuExpanded,
                        onDismissRequest = { gameMenuExpanded = false }
                    ) {
                        games.forEach { game ->
                            DropdownMenuItem(
                                text = { Text(game.name) },
                                onClick = {
                                    selectedGame = game
                                    gameMenuExpanded = false
                                }
                            )
                        }
                        // 「新しいゲームを追加」項目
                        DropdownMenuItem(
                            text = { Text("+ 新しいゲームを追加", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showAddGameDialog = true
                                gameMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // --- 勝者選択 ---
            item {
                Text("勝者", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(players) { player ->
                SelectablePlayerRow(
                    player = player,
                    isSelected = selectedWinners.contains(player),
                    onSelect = {
                        selectedWinners = if (selectedWinners.contains(player)) selectedWinners - player else selectedWinners + player
                        // 勝者と敗者は同時になれないので、もし敗者リストにいたら削除する
                        if (selectedLosers.contains(player)) selectedLosers = selectedLosers - player
                    }
                )
            }

            // --- 敗者選択 ---
            item {
                Text("敗者", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(players) { player ->
                SelectablePlayerRow(
                    player = player,
                    isSelected = selectedLosers.contains(player),
                    onSelect = {
                        selectedLosers = if (selectedLosers.contains(player)) selectedLosers - player else selectedLosers + player
                        // 勝者と敗者は同時になれないので、もし勝者リストにいたら削除する
                        if (selectedWinners.contains(player)) selectedWinners = selectedWinners - player
                    }
                )
            }

            // --- ペナルティ入力 ---
            item {
                OutlinedTextField(
                    value = penaltyDescription,
                    onValueChange = { penaltyDescription = it },
                    label = { Text("ペナルティ (任意)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- 保存ボタン ---
            item {
                Button(
                    onClick = {
                        if (buttonsEnabled && selectedGame != null && selectedWinners.isNotEmpty() && selectedLosers.isNotEmpty()) {
                            buttonsEnabled = false
                            viewModel.saveMatchResult(
                                roomId = roomId,
                                game = selectedGame!!,
                                winners = selectedWinners.toList(),
                                losers = selectedLosers.toList(),
                                penaltyDescription = penaltyDescription
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // ゲーム、勝者、敗者が選ばれていないとボタンを押せないようにする
                    enabled = selectedGame != null && selectedWinners.isNotEmpty() && selectedLosers.isNotEmpty()
                ) {
                    Text("この内容で記録する")
                }
            }
        }

        // --- 「新しいゲームを追加」ダイアログ ---
        if (showAddGameDialog) {
            AlertDialog(
                onDismissRequest = { showAddGameDialog = false },
                title = { Text("新しいゲームを追加") },
                text = {
                    OutlinedTextField(
                        value = newGameName,
                        onValueChange = { newGameName = it },
                        label = { Text("ゲーム名") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addGame(roomId, newGameName)
                        newGameName = ""
                        showAddGameDialog = false
                    }) { Text("追加") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGameDialog = false }) { Text("キャンセル") }
                }
            )
        }
    }
}

// プレイヤー選択の行を共通の部品にする
@Composable
fun SelectablePlayerRow(player: Player, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(player.guestName ?: "アプリユーザー")
    }
}