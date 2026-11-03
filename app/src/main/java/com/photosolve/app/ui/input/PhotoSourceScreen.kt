package com.photosolve.app.ui.input

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun PhotoSourceScreen(
    onImageReady: (Uri) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { isSaved ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (isSaved && uri != null) {
            onImageReady(uri)
        } else {
            message = "没有获取到照片"
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val uri = pendingCameraUri
        if (granted && uri != null) {
            cameraLauncher.launch(uri)
        } else {
            pendingCameraUri = null
            message = "需要相机权限才能拍照"
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) onImageReady(uri) else message = "没有选择图片"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("选择题目图片", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "拍照或从相册选择图片后即可预览",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { galleryLauncher.launch(IMAGE_CONTENT_TYPE) },
                modifier = Modifier.weight(1f),
            ) {
                Text("相册")
            }
            Button(
                onClick = {
                    val uri = createCameraUri(context)
                    pendingCameraUri = uri
                    if (hasCameraPermission(context)) {
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("拍照")
            }
        }
        message?.let { Text(it, modifier = Modifier.padding(top = 16.dp)) }
        Button(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text("返回")
        }
    }
}

private fun hasCameraPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
}

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.filesDir, CAMERA_DIRECTORY).apply { mkdirs() }
    val imageFile = File.createTempFile(CAMERA_FILE_PREFIX, CAMERA_FILE_SUFFIX, directory)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile,
    )
}

private const val IMAGE_CONTENT_TYPE = "image/*"
private const val CAMERA_DIRECTORY = "camera"
private const val CAMERA_FILE_PREFIX = "photo_"
private const val CAMERA_FILE_SUFFIX = ".jpg"
