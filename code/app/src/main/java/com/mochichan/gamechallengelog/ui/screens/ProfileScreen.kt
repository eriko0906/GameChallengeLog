package com.mochichan.gamechallengelog.ui.screens

import android.Manifest // ← これを追加
import android.net.Uri // ← これを追加
import androidx.activity.compose.rememberLauncherForActivityResult // ← これを追加
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image // ← これを追加
import androidx.compose.foundation.clickable // ← これを追加
import androidx.compose.foundation.shape.CircleShape // ← これを追加
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person // ← これを追加
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // ← これを追加
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // ← これを追加
import androidx.compose.ui.layout.ContentScale // ← これを追加
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage // ← これを追加
import com.google.accompanist.permissions.ExperimentalPermissionsApi // ← これを追加
import com.google.accompanist.permissions.rememberPermissionState // ← これを追加
import com.mochichan.gamechallengelog.ui.viewmodels.ProfileViewModel
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.isGranted
import android.content.Intent // ← これを追加
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.platform.LocalContext // ← これを追加

@OptIn(ExperimentalPermissionsApi::class) // ← パーミッション用の実験的機能の利用を許可
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit // ← サインアウト処理を受け取る
) {
    // ViewModelからユーザー情報を取得
    val user by viewModel.user.collectAsState()

    // UIで編集中の名前を保持するための変数
    // LaunchedEffectを使って、DBからの読み込みが完了したら一度だけ名前をセットする
    var editingName by remember { mutableStateOf("") }

    var buttonsEnabled by remember { mutableStateOf(true) }

    // 選択された画像のURIを保持するための変数
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- ↓↓↓ ギャラリーの結果を受け取る部分を、永続化処理を追加して修正します ↓↓↓ ---
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                selectedImageUri = uri
            }
        }
    )

    // ストレージ読み取り許可の状態を管理
    val readPermissionState = rememberPermissionState(
        permission = Manifest.permission.READ_MEDIA_IMAGES
    )


    // --- ↓↓↓ 2つあったLaunchedEffectを1つに統合しました ↓↓↓ ---
    LaunchedEffect(user) {
        user?.let {
            editingName = it.name
            // 画像選択直後はselectedImageUriを上書きしない
            if (selectedImageUri == null && !it.iconUrl.isNullOrBlank()) {
                selectedImageUri = Uri.parse(it.iconUrl)
            }
        }
    }



    Scaffold(topBar = {
        TopAppBar(title = { Text("プロフィール設定") }, navigationIcon = {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // ← 中央揃えを追加
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape) // 円形に切り抜く
                    .clickable {
                        // アイコンがクリックされたときの処理
                        if (readPermissionState.status.isGranted) {
                            // 許可があれば、ギャラリーを起動
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            // 許可がなければ、許可をリクエスト
                            readPermissionState.launchPermissionRequest()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null && selectedImageUri.toString().isNotBlank()) {
                    // 選択された画像があれば、それを表示
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "プロフィール画像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // 画像を円形にフィットさせる
                    )
                } else {
                    // なければ、デフォルトのアイコンを表示
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "プロフィール画像",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 名前の入力欄
            OutlinedTextField(
                value = editingName,
                // --- ↓↓↓ 修正箇所1：入力された文字が10文字を超えたら、それ以上は無視するように修正 ↓↓↓ ---
                onValueChange = {
                    if (it.length <= 10) {
                        editingName = it
                    }
                },
                label = { Text("表示名") },
                modifier = Modifier.fillMaxWidth(),
                // --- ↓↓↓ 修正箇所2：文字数カウンターを表示するように追加 ↓↓↓ ---
                supportingText = {
                    Text(
                        text = "${editingName.length} / 10",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            )

            // 更新ボタン
            Button(
                onClick = {
                    if (buttonsEnabled) {
                        buttonsEnabled = false
                        viewModel.updateUser(editingName, selectedImageUri?.toString())
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("この内容で更新する")
            }

            Spacer(modifier = Modifier.weight(1f)) // 残りのスペースを埋める

            // --- 変更点2：サインアウトボタンを追加します ---
            OutlinedButton(
                onClick = {
                    if (buttonsEnabled) {
                        buttonsEnabled = false
                        onSignOut() // MainActivityから渡されたサインアウト処理を実行
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("サインアウト")
            }
        }
    }
}