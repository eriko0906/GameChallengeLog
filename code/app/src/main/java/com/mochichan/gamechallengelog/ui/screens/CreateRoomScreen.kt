package com.mochichan.gamechallengelog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mochichan.gamechallengelog.ui.viewmodels.RoomListViewModel

@Composable
fun CreateRoomScreen(
    navController: NavController,
    viewModel: RoomListViewModel = viewModel()
) {
    var roomName by remember { mutableStateOf("") }
    var buttonsEnabled by remember { mutableStateOf(true) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("新しいルームの作成") }, navigationIcon = {
            IconButton(onClick = {
                if (buttonsEnabled) {
                    buttonsEnabled = false
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    if (buttonsEnabled && roomName.isNotBlank()) {
                        buttonsEnabled = false
                        viewModel.addRoom(roomName)
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