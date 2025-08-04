package com.mochichan.gamechallengelog.data

import androidx.room.Embedded
import androidx.room.Relation
import java.util.Date

// ペナルティ、担当プレイヤー、そしてそのプレイヤーの最新ユーザー情報を一緒に保持する
data class PenaltyWithPlayer(
    @Embedded val penalty: Penalty,
    @Relation(
        entity = Player::class, // playerテーブルを指定
        parentColumn = "assigneePlayerId",
        entityColumn = "playerId"
    )
    val playerWithDetails: PlayerWithDetails // PlayerからPlayerWithDetailsに変更
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

// プレイヤー情報と、それに紐づくユーザー情報を一緒に保持する
data class PlayerWithDetails(
    @Embedded val player: Player,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: User?
)

// 各プレイヤーの戦績と、そのプレイヤーの最新ユーザー情報を一緒に保持する
data class PlayerStats(
    @Embedded val player: Player, // playerId, playerNameから変更
    val winCount: Int,
    val lossCount: Int,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: User? // user情報を追加
)

// --- ↓↓↓ 新しいデータクラスを2つ追加します ↓↓↓ ---
data class MatchResultWithPlayer(
    @Embedded val result: MatchResult,
    @Relation(
        entity = Player::class,
        parentColumn = "playerId",
        entityColumn = "playerId"
    )
    val playerWithDetails: PlayerWithDetails
)

data class MatchHistoryItem(
    @Embedded val match: Match,
    @Relation(
        entity = Game::class,
        parentColumn = "gameId",
        entityColumn = "gameId"
    )
    val game: Game,
    @Relation(
        entity = MatchResult::class,
        parentColumn = "matchId",
        entityColumn = "matchId"
    )
    val results: List<MatchResultWithPlayer>
)
// --- ↑↑↑ ここまで ---

// ゲームごとの戦績を保持するためのデータクラス
data class GameStats(
    @Embedded val player: Player,
    val winCount: Int,
    val lossCount: Int,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val user: User?
)

// 対戦結果と、それに紐づくプレイヤー（＋最新プロフィール）を一緒に保持する
data class MatchResultWithPlayerDetails(
    @Embedded val result: MatchResult,
    @Relation(
        entity = Player::class,
        parentColumn = "playerId",
        entityColumn = "playerId"
    )
    val playerWithDetails: PlayerWithDetails
)

// 1回の対戦に関する全ての情報（ゲーム、勝敗結果）をまとめて保持する
data class MatchWithDetails(
    @Embedded val match: Match,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "gameId"
    )
    val game: Game,
    @Relation(
        parentColumn = "matchId",
        entityColumn = "matchId",
        entity = MatchResult::class
    )
    val results: List<MatchResultWithPlayerDetails>
)
// --- ↑↑↑ ここまで ---
