package com.mochichan.gamechallengelog.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.data.AppDatabase
import com.mochichan.gamechallengelog.data.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

// アプリ内で、どのユーザーが操作しているかを識別するための仮のID
private const val MOCK_USER_ID = "user_001"

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // 現在操作しているユーザーの情報をUIに渡す
    // MOCK_USER_IDは、将来的にログイン機能などを実装した際に、
    // 実際にログインしているユーザーのIDに置き換える
    val user: StateFlow<User?> = gameDao.getUserById(MOCK_USER_ID)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // --- ↓↓↓ 初期化ロジックを、より安全な方法に修正 ↓↓↓ ---
        viewModelScope.launch {
            // ユーザーが一人もいない（本当に初回起動の）場合のみ、ゲストユーザーを作成する
            if (gameDao.getUserCount() == 0) {
                val guestUser = User(userId = MOCK_USER_ID, name = "ゲスト")
                gameDao.insertOrUpdateUser(guestUser)
            }
        }
    }


    // ユーザー情報を更新（または新規作成）する
    // ユーザー情報を更新（または新規作成）する
    fun updateUser(userName: String, iconUrl: String?) { // iconUrlを受け取るように変更
        viewModelScope.launch {
            val currentUser = user.value ?: User(userId = MOCK_USER_ID, name = "ゲスト")
            // 名前とアイコンURLの両方を更新した、新しいUserオブジェクトを作成
            val updatedUser = currentUser.copy(name = userName, iconUrl = iconUrl)
            gameDao.insertOrUpdateUser(updatedUser)
        }
    }
}