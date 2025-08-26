package com.mochichan.gamechallengelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochichan.gamechallengelog.auth.GoogleAuthUiClient
import com.mochichan.gamechallengelog.auth.SignInResult
import com.mochichan.gamechallengelog.auth.UserData
import com.mochichan.gamechallengelog.ui.screens.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- ↓↓↓ googleAuthUiClientを受け取るように修正します ↓↓↓ ---
class AuthViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient
) : ViewModel() {

    private val _signedInUser = MutableStateFlow<UserData?>(null)
    val signedInUser = _signedInUser.asStateFlow()

    private val _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    init {
        checkInitialSignInState()
    }

    fun checkInitialSignInState() {
        _signedInUser.value = googleAuthUiClient.getSignedInUser()
    }

    fun onSignInResult(result: SignInResult) {
        _signInState.update {
            it.copy(
                isSuccess = result.data != null,
                isError = result.errorMessage,
                isLoading = false
            )
        }
        _signedInUser.value = result.data
    }

    fun resetState() {
        _signInState.update { SignInState() }
    }

    fun setLoading() {
        _signInState.update { it.copy(isLoading = true) }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthUiClient.signOut()
            _signedInUser.value = null
            _signInState.value = SignInState()
        }
    }
}