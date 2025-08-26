package com.mochichan.gamechallengelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.auth.UserData
import com.mochichan.gamechallengelog.data.User
import com.mochichan.gamechallengelog.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = GameRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun loadUser(userData: UserData) {
        viewModelScope.launch {
            // --- ↓↓↓ リアルタイムでユーザー情報を監視するように修正 ↓↓↓ ---
            repository.getUserStream(userData.userId).collect { userFromDb ->
                if (userFromDb == null) {
                    // 初回ログイン時、Firebaseの情報を元に新規作成
                    val newUser = User(
                        userId = userData.userId,
                        name = userData.username ?: "新規ユーザー",
                        iconUrl = userData.profilePictureUrl
                    )
                    // updateUserがファンアウト（関連データの一斉更新）を行う
                    repository.updateUser(newUser)
                    // _user.value = newUser // updateUserがDBを更新すれば、このFlowが自動で検知するので不要
                } else {
                    // データベースの変更をUIに通知する
                    _user.value = userFromDb
                }
            }
        }
    }

    fun updateUser(userName: String, iconUrl: String?) {
        viewModelScope.launch {
            val currentUser = user.value ?: return@launch
            val updatedUser = currentUser.copy(name = userName, iconUrl = iconUrl)
            repository.updateUser(updatedUser)
        }
    }
}