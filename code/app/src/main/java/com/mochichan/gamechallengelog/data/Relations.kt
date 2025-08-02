package com.mochichan.gamechallengelog.data

import androidx.room.Embedded
import androidx.room.Relation
import java.util.Date

// ペナルティ情報と、それに紐づくプレイヤー情報を一緒に保持するためのデータクラス
data class PenaltyWithPlayer(
    @Embedded val penalty: Penalty,
    @Relation(
        parentColumn = "assigneePlayerId",
        entityColumn = "playerId"
    )
    val player: Player
)
data class PlayerStats(
    val playerId: Long,
    val playerName: String?,
    val winCount: Int,
    val lossCount: Int
)
// 対戦履歴を表示するための情報をまとめて保持するデータクラス
data class MatchHistory(
    val matchDate: Date,
    val gameName: String,
    val winnerName: String?
)
// ルーム情報と、そのルームの未完了ペナルティ件数を一緒に保持するためのデータクラス
data class GameRoomWithPenaltyCount(
    @Embedded val gameRoom: GameRoom,
    val penaltyCount: Int
)

data class PlayerWithDetails(
    @Embedded val player: Player,
    // プレイヤーがアプリユーザーの場合、ここにユーザー情報が入る (ゲストの場合はnull)
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: User?
)