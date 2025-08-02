package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.AppDatabase
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.data.GameRoomWithPenaltyCount // ← これを追加
import com.mochichan.gamechallengelog.data.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MOCK_USER_ID = "user_001"

class RoomListViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // --- ↓↓↓ この部分を、新しい命令を使うように修正します ↓↓↓ ---
    val roomsWithPenaltyCount: StateFlow<List<GameRoomWithPenaltyCount>> =
        // MOCK_USER_ID を使って、自分が参加しているルームだけを取得する
        gameDao.getRoomsForUser(MOCK_USER_ID)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    fun addRoom(roomName: String, creator: User) { // userオブジェクトを受け取る
        viewModelScope.launch {
            val newRoom = GameRoom(
                roomId = "room_${System.currentTimeMillis()}",
                name = roomName
            )
            // 新しいトランザクション命令を呼び出す
            gameDao.createRoomAndAddCreatorAsPlayer(newRoom, creator)
        }
    }
}