package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomDetailViewModel(application: Application, val roomId: String) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    val room: StateFlow<GameRoom?> = gameDao.getRoomById(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val playersWithDetails: StateFlow<List<PlayerWithDetails>> = gameDao.getPlayersWithDetailsInRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingPenalties: StateFlow<List<PenaltyWithPlayer>> = gameDao.getPendingPenaltiesForRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ↓↓↓ この対戦履歴の定義が追加されていることが重要です ↓↓↓ ---
    val matchHistory: StateFlow<List<MatchHistory>> = gameDao.getMatchHistoryForRoom(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun addGuestPlayer(roomId: String, guestName: String) {
        viewModelScope.launch {
            if (guestName.isNotBlank()) {
                val newPlayer = Player(
                    roomId = roomId,
                    guestName = guestName
                )
                gameDao.insertPlayer(newPlayer)
            }
        }
    }

    fun completePenalty(penalty: Penalty) {
        viewModelScope.launch {
            val updatedPenalty = penalty.copy(isCompleted = true)
            gameDao.updatePenalty(updatedPenalty)
        }
    }

    fun leaveRoom(userId: String) {
        viewModelScope.launch {
            gameDao.leaveRoomAndDeleteIfLastPlayer(roomId, userId)
        }
    }
}