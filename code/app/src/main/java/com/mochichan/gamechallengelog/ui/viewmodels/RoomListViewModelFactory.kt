package com.mochichan.gamechallengelog.ui.viewmodels


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoomListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}