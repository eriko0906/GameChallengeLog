package com.mochichan.gamechallengelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mochichan.gamechallengelog.ui.screens.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    fun onSignInResult(isSuccess: Boolean, errorMessage: String?) {
        _signInState.update {
            it.copy(
                isSuccess = isSuccess,
                isError = errorMessage,
                isLoading = false
            )
        }
    }

    fun resetState() {
        _signInState.update { SignInState() }
    }

    fun setLoading() {
        _signInState.update { it.copy(isLoading = true) }
    }

    fun getCurrentUser() = auth.currentUser
}