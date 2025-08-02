package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.AppDatabase
import com.mochichan.gamechallengelog.data.Game
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameManagementViewModel(application: Application, roomId: String) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // UIに渡すための、このルームに登録されているゲームのリスト
    val games: StateFlow<List<Game>> = gameDao.getGamesInRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UIから「このゲームを削除して！」とリクエストされたときに実行する
    fun deleteGame(game: Game) {
        viewModelScope.launch {
            gameDao.deleteGame(game)
            // Flowを使っているので、リストは自動で更新されます！
        }
    }
}