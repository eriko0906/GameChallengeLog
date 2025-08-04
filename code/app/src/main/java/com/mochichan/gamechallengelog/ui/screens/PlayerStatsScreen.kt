@file:OptIn(ExperimentalMaterial3Api::class)

package com.mochichan.gamechallengelog.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mochichan.gamechallengelog.data.PlayerStats
import com.mochichan.gamechallengelog.ui.viewmodels.GameSpecificStats
import com.mochichan.gamechallengelog.ui.viewmodels.PlayerStatsViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.Rankings

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
    val rankings by viewModel.rankings.collectAsState()
    var buttonsEnabled by remember { mutableStateOf(true) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("プレイヤー成績") }, navigationIcon = {
            IconButton(onClick = {
                if (buttonsEnabled) {
                    buttonsEnabled = false
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
            }
        })
    }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 総合ランキング ---
            item {
                Text("総合ランキング (勝利回数順)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (rankings.overallStats.isEmpty()) {
                item { Text("まだ対戦記録がありません。") }
            } else {
                items(rankings.overallStats) { stats ->
                    PlayerStatsRow(stats = stats, maxWins = rankings.overallStats.first().winCount)
                }
            }

            // --- ゲーム別ランキング ---
            items(rankings.gameStats) { gameSpecificStats ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${gameSpecificStats.game.name} (${gameSpecificStats.totalPlays}回プレイ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (gameSpecificStats.stats.isEmpty()) {
                    Text("まだ対戦記録がありません。") // ← これでOK
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        gameSpecificStats.stats.forEach { stats ->
                            PlayerStatsRow(stats = stats, maxWins = gameSpecificStats.stats.first().winCount)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerStatsRow(stats: PlayerStats, maxWins: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!stats.user?.iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = stats.user?.iconUrl,
                contentDescription = "プレイヤーアイコン",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "デフォルトアイコン",
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (stats.user != null) {
                    stats.user.name
                } else {
                    "${stats.player.guestName} (ゲスト)"
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "${stats.winCount}勝 / ${stats.lossCount}敗",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
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