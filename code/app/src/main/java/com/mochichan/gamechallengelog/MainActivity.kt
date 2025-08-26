package com.mochichan.gamechallengelog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.identity.Identity
import com.mochichan.gamechallengelog.auth.GoogleAuthUiClient
import com.mochichan.gamechallengelog.auth.UserData
import com.mochichan.gamechallengelog.ui.screens.*
import com.mochichan.gamechallengelog.ui.theme.GameChallengeLog2Theme
import com.mochichan.gamechallengelog.ui.viewmodels.AuthViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import com.mochichan.gamechallengelog.ui.viewmodels.RoomListViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mochichan.gamechallengelog.ui.viewmodels.*
import androidx.navigation.compose.*


// AuthViewModelにgoogleAuthUiClientを渡すための専用の「工場」
class AuthViewModelFactory(private val googleAuthUiClient: GoogleAuthUiClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(googleAuthUiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameChallengeLog2Theme {
                val authViewModel = viewModel<AuthViewModel>(factory = AuthViewModelFactory(googleAuthUiClient))
                val signInState by authViewModel.signInState.collectAsState()
                val signedInUser by authViewModel.signedInUser.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.checkInitialSignInState()
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if (result.resultCode == RESULT_OK) {
                            lifecycleScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                authViewModel.onSignInResult(signInResult)
                            }
                        }
                    }
                )

                if (signedInUser == null) {
                    LaunchedEffect(key1 = signInState.isSuccess) {
                        if (signInState.isSuccess) {
                            Toast.makeText(applicationContext, "サインインしました", Toast.LENGTH_LONG).show()
                        }
                    }

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
                    AppNavigator(
                        userData = signedInUser!!,
                        onSignOut = {
                            authViewModel.signOut()
                            Toast.makeText(applicationContext, "サインアウトしました", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigator(userData: UserData, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val roomListViewModel: RoomListViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    LaunchedEffect(key1 = userData.userId) {
        profileViewModel.loadUser(userData)
        roomListViewModel.loadRoomsForUser(userData)
    }

    NavHost(navController = navController, startDestination = "room_list") {
        composable("room_list") {
            RoomListScreen(
                navController = navController,
                roomListViewModel = roomListViewModel,
                profileViewModel = profileViewModel
            )
        }
        composable("create_room") {
            CreateRoomScreen(
                navController = navController,
                viewModel = roomListViewModel
            )
        }
        composable(
            "room_detail/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            RoomDetailScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId"),
                profileViewModel = profileViewModel
            )
        }
        // ... (他の画面は、一旦ViewModelを受け取らないシンプルな形に戻します)
        composable("join_room") { JoinRoomScreen(navController = navController) }
        composable(
            "game_management/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            GameManagementScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
        composable(
            "record_match/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            RecordMatchScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
        composable(
            "player_stats/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            PlayerStatsScreen(
                navController = navController,
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                onSignOut = onSignOut
            )
        }
    }
}