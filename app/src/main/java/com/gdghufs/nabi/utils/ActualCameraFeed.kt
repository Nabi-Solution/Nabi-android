package com.gdghufs.nabi.utils

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

@Composable
fun ActualCameraFeed(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // 전면 카메라 기본값
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = PreviewView.ScaleType.FILL_CENTER // 프리뷰 화면 채우기 방식
        }
    }

    LaunchedEffect(lifecycleOwner, cameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            try {
                cameraProvider.unbindAll() // 기존 바인딩 해제
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                Log.e("ActualCameraFeed", "카메라 사용 준비 실패", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
fun SelfieCameraView(modifier: Modifier = Modifier) { // 부모가 전달하는 레이아웃 조정용 modifier
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
        }
    )

    Box(
        modifier = modifier // ChatScreenContent 에서 전달된 Modifier (align, padding 등)
            .size(100.dp)     // 셀피 뷰 자체 크기
            .clip(CircleShape)
            .background(Color.DarkGray) // 카메라 로딩 전 또는 권한 없을 때 배경
            .border(2.dp, Color.LightGray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (hasPermission) {
            ActualCameraFeed(
                modifier = Modifier.fillMaxSize(), // Box 크기에 맞춰 카메라 피드 채우기
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // 전면 카메라 사용
            )
        } else {
            // 권한이 없을 때 보여줄 UI (예: 권한 요청 버튼)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "카메라 권한 필요",
                    tint = Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { permissionLauncher.launch(CAMERA_PERMISSION) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("권한 요청", fontSize = 10.sp)
                }
            }
        }
    }

    // SelfieCameraView가 처음 실행될 때 권한이 없다면 요청 (선택적)
    // 사용자가 버튼을 통해 명시적으로 요청하도록 두어도 됩니다.
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            // 필요하다면 여기서 바로 권한 요청: permissionLauncher.launch(CAMERA_PERMISSION)
            // 현재는 버튼을 통한 요청으로 남겨둡니다.
        }
    }
}