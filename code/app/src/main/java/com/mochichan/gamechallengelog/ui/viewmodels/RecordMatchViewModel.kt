package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class RecordMatchViewModel(application: Application, roomId: String) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // UIに渡すためのデータ
    val players: StateFlow<List<Player>> = gameDao.getPlayersInRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val games: StateFlow<List<Game>> = gameDao.getGamesInRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 新しいゲームをデータベースに追加する
    fun addGame(roomId: String, gameName: String) {
        viewModelScope.launch {
            if (gameName.isNotBlank()) {
                val newGame = Game(roomId = roomId, name = gameName)
                gameDao.insertGame(newGame)
            }
        }
    }

    // 対戦結果を保存する
    fun saveMatchResult(
        roomId: String,
        game: Game,
        winners: List<Player>,
        losers: List<Player>,
        penaltyDescription: String
    ) {
        viewModelScope.launch {
            // 1. 対戦記録を保存し、そのIDを取得する
            val newMatch = Match(roomId = roomId, gameId = game.gameId, matchDate = Date())
            val matchId = gameDao.insertMatch(newMatch)

            // 2. 勝者の結果を保存する
            winners.forEach { winner ->
                val result = MatchResult(matchId = matchId, playerId = winner.playerId, outcome = "win")
                gameDao.insertMatchResult(result)
            }

            // 3. 敗者の結果とペナルティを保存する
            losers.forEach { loser ->
                val result = MatchResult(matchId = matchId, playerId = loser.playerId, outcome = "loss")
                gameDao.insertMatchResult(result)

                if (penaltyDescription.isNotBlank()) {
                    val penalty = Penalty(
                        matchId = matchId,
                        assigneePlayerId = loser.playerId,
                        description = penaltyDescription,
                        isCompleted = false
                    )
                    gameDao.insertPenalty(penalty)
                }
            }
        }
    }
}