package com.mochichan.gamechallengelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween // ← これを追加
import androidx.compose.animation.fadeIn // ← これを追加
import androidx.compose.animation.fadeOut // ← これを追加
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mochichan.gamechallengelog.ui.screens.*
import com.mochichan.gamechallengelog.ui.theme.GameChallengeLog2Theme
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.RoomListViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.mochichan.gamechallengelog.ui.viewmodels.AuthViewModel
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.getValue
import com.mochichan.gamechallengelog.auth.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient: GoogleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameChallengeLog2Theme {
                val authViewModel = viewModel<AuthViewModel>()
                val signInState by authViewModel.signInState.collectAsState()

                // ユーザーがログイン成功したら、次の画面に遷移する
                LaunchedEffect(key1 = signInState.isSuccess) {
                    if (signInState.isSuccess) {
                        Toast.makeText(applicationContext, "サインインしました", Toast.LENGTH_LONG).show()
                        // ここでメインのナビゲーションに切り替える
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if (result.resultCode == RESULT_OK) {
                            lifecycleScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                authViewModel.onSignInResult(
                                    isSuccess = signInResult.data != null,
                                    errorMessage = signInResult.errorMessage
                                )
                            }
                        }
                    }
                )

                // ログイン状態に応じて表示を切り替え
                if (googleAuthUiClient.getSignedInUser() == null) {
                    LoginScreen(
                        state = signInState,
                        onSignInClick = {
                            authViewModel.setLoading()
                            lifecycleScope.launch {
                                val signInIntentSender = googleAuthUiClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch
                                    ).build()
                                )
                            }
                        }
                    )
                } else {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val roomListViewModel: RoomListViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    // ↓↓↓ アニメーションをゼロにする設定を追加 ↓↓↓
    NavHost(
        navController = navController,
        startDestination = "room_list",
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable("room_list") {
            // --- ↓↓↓ ViewModelを渡すように修正 ↓↓↓ ---
            RoomListScreen(
                navController = navController,
                roomListViewModel = roomListViewModel,
                profileViewModel = profileViewModel
            )
        }
        composable(
            route = "room_detail/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            // 渡されてきたroomIdを取得し、RoomDetailScreenに渡す
            RoomDetailScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId"),
                profileViewModel = profileViewModel
            )
        }
        composable("create_room") {
            // ↓↓↓ ProfileViewModelを渡すように修正 ↓↓↓
            CreateRoomScreen(
                navController = navController,
                viewModel = roomListViewModel,
                profileViewModel = profileViewModel // ← profileViewModelを渡す
            )
        }
        composable("join_room") { JoinRoomScreen(navController = navController) }
        composable(
            route = "game_management/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            GameManagementScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
        composable(
            route = "record_match/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            RecordMatchScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
        composable(
            route = "player_stats/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            PlayerStatsScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
        composable("profile") {
            // --- ↓↓↓ ViewModelを渡すように修正 ↓↓↓ ---
            ProfileScreen(navController = navController, viewModel = profileViewModel)
        }
    }
}