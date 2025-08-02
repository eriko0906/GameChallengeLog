package com.mochichan.gamechallengelog.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.mochichan.gamechallengelog.ui.viewmodels.GameManagementViewModel

// ViewModelを生成するための「工場」
class GameManagementViewModelFactory(private val application: Application, private val roomId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameManagementViewModel(application, roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun GameManagementScreen(navController: NavController, roomId: String?) {
    if (roomId == null) {
        Text("エラー：ルームIDが見つかりません。")
        return
    }

    val application = LocalContext.current.applicationContext as Application
    val viewModel: GameManagementViewModel = viewModel(factory = GameManagementViewModelFactory(application, roomId))
    val games by viewModel.games.collectAsState()
    var buttonsEnabled by remember { mutableStateOf(true) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("ゲーム管理") }, navigationIcon = {
            IconButton(onClick = {
                if (buttonsEnabled) {
                    buttonsEnabled = false
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, "戻る")
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("登録済みのゲーム", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (games.isEmpty()) {
                Text("まだゲームが登録されていません。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(games) { game ->
                        GameRow(
                            game = game,
                            onDelete = {
                                // ViewModelに削除を依頼
                                viewModel.deleteGame(game)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ゲーム一行分のUIを部品として定義
@Composable
fun GameRow(game: Game, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(game.name)
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "削除")
        }
    }
    Divider() // 区切り線
}