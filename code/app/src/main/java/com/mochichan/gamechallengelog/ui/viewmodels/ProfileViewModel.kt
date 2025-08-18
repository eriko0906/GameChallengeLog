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
import com.mochichan.gamechallengelog.auth.UserData // ← これを追加
import kotlinx.coroutines.flow.*



class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val gameDao = AppDatabase.getInstance(application).gameDao()

    // --- ↓↓↓ この部分を、ログインユーザーに追従するように変更 ↓↓↓ ---
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun loadUser(userData: UserData) {
        viewModelScope.launch {
            gameDao.getUserById(userData.userId).collect { userFromDb ->
                if (userFromDb == null) {
                    // DBにユーザーが存在しない場合（初回ログイン時）、Firebaseの情報を元に新規作成
                    val newUser = User(
                        userId = userData.userId,
                        name = userData.username ?: "新規ユーザー",
                        iconUrl = userData.profilePictureUrl
                    )
                    gameDao.insertOrUpdateUser(newUser)
                    _user.value = newUser
                } else {
                    _user.value = userFromDb
                }
            }
        }
    }

    // ユーザー情報を更新（または新規作成）する
    // ユーザー情報を更新（または新規作成）する
    fun updateUser(userName: String, iconUrl: String?) { // iconUrlを受け取るように変更
        viewModelScope.launch {
            val currentUser = user.value ?: return@launch // ユーザーがいなければ何もしない
            val updatedUser = currentUser.copy(name = userName, iconUrl = iconUrl)
            gameDao.insertOrUpdateUser(updatedUser)
        }
    }
}