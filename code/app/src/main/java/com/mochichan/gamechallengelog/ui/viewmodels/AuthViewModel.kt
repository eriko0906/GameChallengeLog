package com.mochichan.gamechallengelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mochichan.gamechallengelog.ui.screens.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.mochichan.gamechallengelog.auth.GoogleAuthUiClient
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.mochichan.gamechallengelog.auth.UserData

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    // --- ↓↓↓ 修正箇所1：現在のサインイン状態を管理する変数を追加します ↓↓↓ ---
    private val _signedInUser = MutableStateFlow<UserData?>(null)
    val signedInUser = _signedInUser.asStateFlow()
    // --- ↑↑↑ ここまで ---

    private val _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    // --- ↓↓↓ 修正箇所2：「checkInitialSignInState」という新しい関数を追加します ↓↓↓ ---
    // アプリ起動時に、一度だけサインイン状態を確認します
    fun checkInitialSignInState(googleAuthUiClient: GoogleAuthUiClient) {
        _signedInUser.value = googleAuthUiClient.getSignedInUser()
    }

    fun onSignInResult(
        isSuccess: Boolean,
        errorMessage: String?,
        googleAuthUiClient: GoogleAuthUiClient // ← 追加
    ) {
        _signInState.update {
            it.copy(
                isSuccess = isSuccess,
                isError = errorMessage,
                isLoading = false
            )
        }
        // サインイン結果が出たら、ユーザー情報を更新してUIに通知します
        _signedInUser.value = if (isSuccess) googleAuthUiClient.getSignedInUser() else null
    }

    fun resetState() {
        _signInState.update { SignInState() }
    }

    fun setLoading() {
        _signInState.update { it.copy(isLoading = true) }
    }

    // --- ↓↓↓ サインアウト処理をここに追加します ↓↓↓ ---
    fun signOut(googleAuthUiClient: GoogleAuthUiClient) {
        viewModelScope.launch {
            googleAuthUiClient.signOut()
            // サインアウトしたら、ユーザー情報をnullにしてUIに通知します
            _signedInUser.value = null
            _signInState.update { SignInState() } // 状態を完全にリセット
        }
    }
}