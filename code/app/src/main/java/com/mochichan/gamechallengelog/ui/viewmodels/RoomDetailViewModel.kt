package com.mochichan.gamechallengelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.GameRoom
import com.mochichan.gamechallengelog.data.Player
import com.mochichan.gamechallengelog.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomDetailViewModel(private val roomId: String) : ViewModel() {

    private val repository = GameRepository()

    private val _room = MutableStateFlow<GameRoom?>(null)
    val room: StateFlow<GameRoom?> = _room.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    init {
        // ViewModelが作られた瞬間に、ルームとプレイヤーの情報をリアルタイムで監視し始める
        viewModelScope.launch {
            repository.getRoomStream(roomId).collect { roomData ->
                _room.value = roomData
            }
        }
        viewModelScope.launch {
            repository.getPlayersStream(roomId).collect { playerData ->
                _players.value = playerData
            }
        }
    }

    // UIから「ゲストを追加して！」とリクエストされたときに実行する
    fun addGuestPlayer(guestName: String) {
        viewModelScope.launch {
            repository.addGuestPlayer(roomId, guestName)
        }
    }
}