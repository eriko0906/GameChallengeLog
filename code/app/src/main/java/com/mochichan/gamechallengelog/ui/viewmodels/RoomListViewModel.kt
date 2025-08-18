package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.auth.UserData
import com.mochichan.gamechallengelog.data.AppDatabase
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.data.GameRoomWithPenaltyCount // ← これを追加
import com.mochichan.gamechallengelog.data.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.mochichan.gamechallengelog.data.*
import kotlinx.coroutines.flow.*

class RoomListViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // --- ↓↓↓ この部分を、ログインユーザーのルームだけ表示するように変更 ↓↓↓ ---
    private val _roomsWithPenaltyCount = MutableStateFlow<List<GameRoomWithPenaltyCount>>(emptyList())
    val roomsWithPenaltyCount: StateFlow<List<GameRoomWithPenaltyCount>> = _roomsWithPenaltyCount.asStateFlow()

    fun loadRoomsForUser(user: UserData) {
        viewModelScope.launch {
            gameDao.getRoomsForUser(user.userId).collect { rooms ->
                _roomsWithPenaltyCount.value = rooms
            }
        }
    }


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