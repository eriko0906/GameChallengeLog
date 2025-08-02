@file:OptIn(ExperimentalMaterial3Api::class)

package com.mochichan.gamechallengelog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mochichan.gamechallengelog.ui.viewmodels.RoomListViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel

@Composable
fun CreateRoomScreen(
    navController: NavController,
    // --- ↓↓↓ ViewModelを引数として受け取るように変更 ↓↓↓ ---
    viewModel: RoomListViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var roomName by remember { mutableStateOf("") }
    var buttonsEnabled by remember { mutableStateOf(true) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val user by profileViewModel.user.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("新しいルームの作成") }, navigationIcon = {
            IconButton(onClick = {
                if (buttonsEnabled) {
                    buttonsEnabled = false // ボタンを即座に無効化
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, "戻る")
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("ルーム名") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    // --- ↓↓↓ ボタンが押された時の処理を修正 ↓↓↓ ---
                    if (buttonsEnabled && roomName.isNotBlank()) {
                        buttonsEnabled = false // ボタンを即座に無効化
                        viewModel.addRoom(roomName, user!!) // ユーザー情報を渡す
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("この内容でルームを作成する")
            }
        }
    }
}