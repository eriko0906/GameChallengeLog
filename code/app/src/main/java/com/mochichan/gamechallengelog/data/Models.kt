package com.mochichan.gamechallengelog.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String,
    val iconUrl: String? = null
)

@Entity(tableName = "game_rooms")
data class GameRoom(
    @PrimaryKey val roomId: String,
    val name: String
)

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val playerId: Long = 0,
    val roomId: String,
    val userId: String? = null,
    val guestName: String? = null
)

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val gameId: Long = 0,
    val roomId: String,
    val name: String
)

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val matchId: Long = 0,
    val roomId: String,
    val gameId: Long,
    val matchDate: Date
)

@Entity(tableName = "match_results")
data class MatchResult(
    @PrimaryKey(autoGenerate = true) val resultId: Long = 0,
    val matchId: Long,
    val playerId: Long,
    val outcome: String
)

@Entity(tableName = "penalties")
data class Penalty(
    @PrimaryKey(autoGenerate = true) val penaltyId: Long = 0,
    val matchId: Long,
    val assigneePlayerId: Long,
    val description: String,
    val isCompleted: Boolean
)

@Entity(tableName = "penalty_templates")
data class PenaltyTemplate(
    @PrimaryKey(autoGenerate = true) val templateId: Long = 0,
    val roomId: String,
    val description: String
)