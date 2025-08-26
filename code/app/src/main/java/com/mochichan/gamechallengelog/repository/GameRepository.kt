package com.mochichan.gamechallengelog.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mochichan.gamechallengelog.data.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import android.util.Log // ← 不足していたimport文

private const val TAG = "GameRepository" // ← Logで使うためのタグ

class GameRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // --- User ---
    suspend fun getUser(userId: String): User? {
        return try {
            db.collection("users").document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun updateUser(user: User) {
        try {
            // 1. ユーザーのプロフィール（usersコレクション）を更新
            db.collection("users").document(user.userId).set(user).await()

            // 2. 参加している全ルームを取得
            val roomsQuery = db.collection("game_rooms")
                .whereArrayContains("memberIds", user.userId)
                .get()
                .await()

            // 3. 各ルームのplayersサブコレクションの該当ユーザー情報を更新
            db.runBatch { batch ->
                for (roomDoc in roomsQuery.documents) {
                    val playerRef = roomDoc.reference.collection("players").document(user.userId)
                    batch.update(playerRef, mapOf(
                        "name" to user.name,
                        "iconUrl" to user.iconUrl
                    ))
                }
            }.await()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating user and player profiles", e)
        }
    }

    // --- GameRoom ---
    suspend fun createRoom(roomName: String) {
        val currentUser = auth.currentUser ?: return
        val userProfile = getUser(currentUser.uid) ?: return

        val newRoomRef = db.collection("game_rooms").document()
        val newRoom = GameRoom(
            roomId = newRoomRef.id,
            name = roomName,
            memberIds = listOf(currentUser.uid)
        )
        try {
            db.runBatch { batch ->
                batch.set(newRoomRef, newRoom)
                val creatorAsPlayer = Player(
                    userId = userProfile.userId,
                    name = userProfile.name,
                    iconUrl = userProfile.iconUrl,
                    isGuest = false
                )
                val newPlayerRef = newRoomRef.collection("players").document(currentUser.uid)
                batch.set(newPlayerRef, creatorAsPlayer)
            }.await()
        } catch (e: Exception) { /* ... */ }
    }

    fun getRoomsForCurrentUser(): Flow<List<GameRoom>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList()); awaitClose(); return@callbackFlow
        }
        val listener = db.collection("game_rooms")
            .whereArrayContains("memberIds", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                trySend(snapshot.toObjects(GameRoom::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun getRoomStream(roomId: String): Flow<GameRoom?> = callbackFlow {
        val listener = db.collection("game_rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null); return@addSnapshotListener
                }
                trySend(snapshot.toObject(GameRoom::class.java))
            }
        awaitClose { listener.remove() }
    }

    // --- Player ---
    fun getPlayersStream(roomId: String): Flow<List<Player>> = callbackFlow {
        val listener = db.collection("game_rooms").document(roomId).collection("players")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList()); return@addSnapshotListener
                }
                trySend(snapshot.toObjects(Player::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun addGuestPlayer(roomId: String, guestName: String) {
        val newPlayerRef = db.collection("game_rooms").document(roomId).collection("players").document()
        val newPlayer = Player(
            userId = newPlayerRef.id,
            name = guestName,
            isGuest = true,
            guestName = guestName
        )
        try {
            newPlayerRef.set(newPlayer).await()
        } catch (e: Exception) { /* ... */ }
    }

    // ユーザー情報をリアルタイムで監視するための命令
    fun getUserStream(userId: String): Flow<User?> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }
}