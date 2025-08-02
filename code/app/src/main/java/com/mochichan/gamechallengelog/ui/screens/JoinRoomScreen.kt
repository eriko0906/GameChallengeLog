@file:OptIn(ExperimentalMaterial3Api::class)

package com.mochichan.gamechallengelog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun JoinRoomScreen(navController: NavController) {
    var roomId by remember { mutableStateOf("") }
    var buttonsEnabled by remember { mutableStateOf(true) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("ルームに参加する") }, navigationIcon = {
            IconButton(onClick = {
                if (buttonsEnabled) {
                    buttonsEnabled = false
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, "戻る")
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = roomId, onValueChange = { roomId = it }, label = { Text("ルームID") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { /*TODO*/; navController.navigate("room_detail") }, modifier = Modifier.fillMaxWidth()) {
                Text("このIDのルームに参加する")
            }
        }
    }
}