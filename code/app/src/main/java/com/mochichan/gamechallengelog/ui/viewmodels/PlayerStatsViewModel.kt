package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.*
import kotlinx.coroutines.flow.*

// UIに表示するための、完成されたランキング情報を保持する
data class Rankings(
    val overallStats: List<PlayerStats> = emptyList(),
    val gameStats: List<GameSpecificStats> = emptyList()
)

data class GameSpecificStats(
    val game: Game,
    val totalPlays: Int,
    val stats: List<PlayerStats>
)

class PlayerStatsViewModel(application: Application, roomId: String) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    val rankings: StateFlow<Rankings> = gameDao.getAllMatchesWithDetails(roomId)
        .map { allMatches ->
            val allPlayersInRoom = gameDao.getPlayersWithDetailsInRoom(roomId).first()

            // 1. 総合ランキングの計算
            val overallStats = allPlayersInRoom.map { playerDetails ->
                PlayerStats(
                    player = playerDetails.player,
                    winCount = allMatches.flatMap { it.results }.count { it.playerWithDetails.player.playerId == playerDetails.player.playerId && it.result.outcome == "win" },
                    lossCount = allMatches.flatMap { it.results }.count { it.playerWithDetails.player.playerId == playerDetails.player.playerId && it.result.outcome == "loss" },
                    user = playerDetails.user
                )
            }.sortedByDescending { it.winCount }

            // 2. ゲーム別ランキングの計算
            val gameStats = allMatches
                .groupBy { it.game }
                .map { (game, matches) ->
                    val statsForGame = allPlayersInRoom.map { playerDetails ->
                        PlayerStats(
                            player = playerDetails.player,
                            winCount = matches.flatMap { it.results }.count { it.playerWithDetails.player.playerId == playerDetails.player.playerId && it.result.outcome == "win" },
                            lossCount = matches.flatMap { it.results }.count { it.playerWithDetails.player.playerId == playerDetails.player.playerId && it.result.outcome == "loss" },
                            user = playerDetails.user
                        )
                    }.sortedByDescending { it.winCount }

                    GameSpecificStats(
                        game = game,
                        totalPlays = matches.size,
                        stats = statsForGame
                    )
                }
                .sortedByDescending { it.totalPlays }

            Rankings(
                overallStats = overallStats,
                gameStats = gameStats
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Rankings()
        )
}