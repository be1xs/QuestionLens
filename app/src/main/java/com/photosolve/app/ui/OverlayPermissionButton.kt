package com.photosolve.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.photosolve.app.capture.ACTION_START_OVERLAY
import com.photosolve.app.capture.FloatingOverlayService
import com.photosolve.app.capture.OverlayState
import com.photosolve.app.capture.OverlayStatus

@Composable
fun rememberStartOverlay(onMessage: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val start = {
        try {
            OverlayState.update(OverlayStatus.STARTING)
            ContextCompat.startForegroundService(
                context, Intent(context, FloatingOverlayService::class.java).setAction(ACTION_START_OVERLAY),
            )
        } catch (error: Exception) {
            OverlayState.update(OverlayStatus.OFF)
            onMessage("无法启动悬浮窗，请在系统设置中允许悬浮窗和后台运行后重试。")
        }
    }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        start()
    }
    val startWithNotification = {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else start()
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(context)) startWithNotification()
        else onMessage("尚未允许显示悬浮窗，可在系统设置中开启后重试。")
    }
    return {
        if (Settings.canDrawOverlays(context)) startWithNotification()
        else try {
            launcher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
        } catch (error: Exception) {
            onMessage("无法打开权限页面，请在手机设置 → 应用 → 拍题助手中允许悬浮窗。")
        }
    }
}
