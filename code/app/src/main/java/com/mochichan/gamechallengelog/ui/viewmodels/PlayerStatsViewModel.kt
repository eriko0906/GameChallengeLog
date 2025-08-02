package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.AppDatabase
import com.mochichan.gamechallengelog.data.PlayerStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PlayerStatsViewModel(application: Application, roomId: String) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // UIに渡すための、このルームの全プレイヤーの戦績リスト
    val playerStats: StateFlow<List<PlayerStats>> = gameDao.getPlayerStatsInRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}