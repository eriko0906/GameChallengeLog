package com.mochichan.gamechallengelog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date
import kotlinx.coroutines.flow.first

@Dao
interface GameDao {

    // --- Room (ルーム) に関する命令 ---
    @Insert
    suspend fun insertRoom(gameRoom: GameRoom)

    // ↓↓↓ この命令を、ユーザーIDで絞り込むように修正 ↓↓↓
    @Transaction
    @Query("""
        SELECT gr.*, (
            SELECT COUNT(*) FROM penalties p
            INNER JOIN matches m ON p.matchId = m.matchId
            WHERE m.roomId = gr.roomId AND p.isCompleted = 0
        ) as penaltyCount
        FROM game_rooms gr
        INNER JOIN players p ON gr.roomId = p.roomId
        WHERE p.userId = :userId
    """)
    fun getRoomsForUser(userId: String): Flow<List<GameRoomWithPenaltyCount>>

    @Transaction
    suspend fun leaveRoomAndDeleteIfLastPlayer(roomId: String, userId: String) {
        // プレイヤーを削除
        deletePlayerByUser(roomId, userId)
        // 残りのプレイヤー数を確認
        val remainingPlayers = getPlayersWithDetailsInRoom(roomId)
        // Flow<List<Player>> なので、最初の値を取得
        val playersList = remainingPlayers.first()
        if (playersList.isEmpty()) {
            // 最後のプレイヤーならルームも削除
            deleteRoomById(roomId)
        }
    }

    @Query("SELECT * FROM players WHERE roomId = :roomId")
    fun getPlayersInRoom(roomId: String): Flow<List<Player>>

    @Query("SELECT * FROM game_rooms WHERE roomId = :id")
    fun getRoomById(id: String): Flow<GameRoom?>

    // ↓↓↓ ルーム削除の命令を追加 ↓↓↓
    @Query("DELETE FROM game_rooms WHERE roomId = :roomId")
    suspend fun deleteRoomById(roomId: String)

    // ... (以降の命令は変更なし) ...
    @Insert
    suspend fun insertPlayer(player: Player)

    // --- ↓↓↓ getPlayersInRoom 命令を、新しい命令に置き換えます ↓↓↓ ---
    @Transaction
    @Query("SELECT * FROM players WHERE roomId = :roomId")
    fun getPlayersWithDetailsInRoom(roomId: String): Flow<List<PlayerWithDetails>>
    // --- ↑↑↑ ここまで修正 ---

    @Insert
    suspend fun insertGame(game: Game)

    @Query("SELECT * FROM games WHERE roomId = :roomId")
    fun getGamesInRoom(roomId: String): Flow<List<Game>>

    @Delete
    suspend fun deleteGame(game: Game)

    @Insert
    suspend fun insertMatch(match: Match): Long

    @Insert
    suspend fun insertMatchResult(result: MatchResult)

    @Insert
    suspend fun insertPenalty(penalty: Penalty)

    @Update
    suspend fun updatePenalty(penalty: Penalty)

    @Transaction
    @Query("""
        SELECT * FROM penalties WHERE isCompleted = 0 AND matchId IN 
        (SELECT matchId FROM matches WHERE roomId = :roomId)
    """)
    fun getPendingPenaltiesForRoom(roomId: String): Flow<List<PenaltyWithPlayer>>

    @Transaction
    @Query("""
    SELECT 
        p.*, 
        SUM(CASE WHEN mr.outcome = 'win' THEN 1 ELSE 0 END) as winCount,
        SUM(CASE WHEN mr.outcome = 'loss' THEN 1 ELSE 0 END) as lossCount
    FROM players p
    LEFT JOIN match_results mr ON p.playerId = mr.playerId
    WHERE p.roomId = :roomId
    GROUP BY p.playerId
""")
    fun getPlayerStatsInRoom(roomId: String): Flow<List<PlayerStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: User)

    @Query("SELECT * FROM users WHERE userId = :id")
    fun getUserById(id: String): Flow<User?>

    @Transaction
    @Query("""
        SELECT * FROM matches
        WHERE roomId = :roomId
        ORDER BY matchDate DESC
        LIMIT 5
    """)
    fun getMatchHistoryItemsForRoom(roomId: String): Flow<List<MatchHistoryItem>>

    @Transaction
    suspend fun createRoomAndAddCreatorAsPlayer(gameRoom: GameRoom, user: User) {
        // 1. まず、ユーザー情報が最新であることを保証する
        insertOrUpdateUser(user)
        // 2. 新しいルームを作成する
        insertRoom(gameRoom)
        // 3. そのユーザーを、新しいルームのプレイヤーとして追加する
        val creatorAsPlayer = Player(roomId = gameRoom.roomId, userId = user.userId, guestName = user.name)
        insertPlayer(creatorAsPlayer)
    }

    @Query("DELETE FROM players WHERE roomId = :roomId AND userId = :userId")
    suspend fun deletePlayerByUser(roomId: String, userId: String)
}