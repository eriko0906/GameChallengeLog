package com.mochichan.gamechallengelog.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Firestore用のデータクラス

data class User(
    val userId: String = "",
    val name: String = "",
    val iconUrl: String? = null
)

data class GameRoom(
    val roomId: String = "",
    val name: String = "",
    val memberIds: List<String> = emptyList() // このルームに参加している全ユーザーのIDリスト
)

// Playerは、各ルームの「サブコレクション」として保存します
data class Player(
    val userId: String = "", // GoogleサインインのID
    val name: String = "",   // ユーザー名（非正規化）
    val iconUrl: String? = null,
    val isGuest: Boolean = false,
    val guestName: String? = null
)

data class Game(
    val gameId: String = "",
    val name: String = ""
)

data class Match(
    val matchId: String = "",
    val gameId: String = "",
    val gameName: String = "",
    @ServerTimestamp val matchDate: Date? = null,
    val winnerIds: List<String> = emptyList(),
    val loserIds: List<String> = emptyList()
)

data class Penalty(
    val penaltyId: String = "",
    val matchId: String = "",
    val assigneePlayerId: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null
)