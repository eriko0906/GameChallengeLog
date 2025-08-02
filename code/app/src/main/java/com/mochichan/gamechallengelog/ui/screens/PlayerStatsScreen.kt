package com.mochichan.gamechallengelog.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mochichan.gamechallengelog.data.PlayerStats
import com.mochichan.gamechallengelog.ui.viewmodels.PlayerStatsViewModel

// ViewModelを生成するための「工場」
class PlayerStatsViewModelFactory(private val application: Application, private val roomId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerStatsViewModel(application, roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun PlayerStatsScreen(navController: NavController, roomId: String?) {
    if (roomId == null) {
        Text("エラー：ルームIDが見つかりません。")
        return
    }

    val application = LocalContext.current.applicationContext as Application
    val viewModel: PlayerStatsViewModel = viewModel(factory = PlayerStatsViewModelFactory(application, roomId))
    val playerStats by viewModel.playerStats.collectAsState()
    var buttonsEnabled by remember { mutableStateOf(true) }

    // 勝利数で降順にソート
    val sortedStats = playerStats.sortedByDescending { it.winCount }

    Scaffold(topBar = {
        TopAppBar(title = { Text("プレイヤー成績") }, navigationIcon = {
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
            Text("総合ランキング (勝利回数順)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (sortedStats.isEmpty()) {
                Text("まだ対戦記録がありません。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    itemsIndexed(sortedStats) { index, stats ->
                        PlayerStatsRow(rank = index + 1, stats = stats, maxWins = sortedStats.first().winCount)
                    }
                }
            }
        }
    }
}

// 成績一行分のUIを部品として定義
@Composable
fun PlayerStatsRow(rank: Int, stats: PlayerStats, maxWins: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${rank}.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(stats.playerName ?: "アプリユーザー", style = MaterialTheme.typography.bodyLarge)
            Text(
                "${stats.winCount}勝 / ${stats.lossCount}敗",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 簡易的な棒グラフ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = if (maxWins > 0) stats.winCount.toFloat() / maxWins.toFloat() else 0f)
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}