package com.mochichan.gamechallengelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.auth.UserData
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoomListViewModel : ViewModel() {
    private val repository = GameRepository()
    private val _rooms = MutableStateFlow<List<GameRoom>>(emptyList())
    val rooms: StateFlow<List<GameRoom>> = _rooms.asStateFlow()

    fun loadRoomsForUser(user: UserData) {
        viewModelScope.launch {
            repository.getRoomsForCurrentUser().collect { roomList ->
                _rooms.value = roomList
            }
        }
    }

    fun addRoom(roomName: String) {
        viewModelScope.launch {
            repository.createRoom(roomName)
        }
    }
}