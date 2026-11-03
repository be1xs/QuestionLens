package com.photosolve.app.capture

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.photosolve.app.MainActivity

class ScreenCapturePermissionActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var completed = false
    private val timeout = Runnable { finishCapture(null, "截图超时，请重新点击识屏。") }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finishCapture(intent?.getStringExtra(EXTRA_SCREENSHOT_URI), intent?.getStringExtra(EXTRA_CAPTURE_ERROR))
        }
    }
    private val permission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) {
            finishCapture(null, null)
        } else {
            try {
                ContextCompat.startForegroundService(this, Intent(this, ScreenCaptureService::class.java)
                    .putExtra(EXTRA_RESULT_CODE, result.resultCode).putExtra(EXTRA_RESULT_DATA, data))
                handler.postDelayed(timeout, CAPTURE_TIMEOUT_MS)
            } catch (error: Exception) {
                finishCapture(null, "无法开始截图，请重新授权并检查后台运行权限。")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.registerReceiver(this, receiver, IntentFilter(ACTION_CAPTURE_FINISHED), ContextCompat.RECEIVER_NOT_EXPORTED)
        onBackPressedDispatcher.addCallback(this) { finishCapture(null, null) }
        try {
            startService(Intent(this, FloatingOverlayService::class.java).setAction(ACTION_HIDE_OVERLAY))
            if (savedInstanceState == null) {
                val manager = getSystemService(MediaProjectionManager::class.java)
                val request = if (Build.VERSION.SDK_INT >= 34) {
                    manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
                } else manager.createScreenCaptureIntent()
                permission.launch(request)
            } else handler.postDelayed(timeout, CAPTURE_TIMEOUT_MS)
        } catch (error: Exception) {
            finishCapture(null, "无法打开截图授权，请检查系统权限。")
        }
    }

    private fun finishCapture(uri: String?, error: String?) {
        if (completed) return
        completed = true
        handler.removeCallbacks(timeout)
        if (uri != null) {
            startActivity(Intent(this, MainActivity::class.java).setAction(ACTION_SCREENSHOT_READY)
                .putExtra(EXTRA_SCREENSHOT_URI, uri)
                // Return to the main task so this temporary capture task can finish cleanly.
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        } else if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        stopService(Intent(this, ScreenCaptureService::class.java))
        try { startService(Intent(this, FloatingOverlayService::class.java).setAction(ACTION_RESTORE_OVERLAY)) }
        catch (failure: IllegalStateException) {
            Toast.makeText(this, "请回到拍题助手重新开启悬浮窗。", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private companion object { const val CAPTURE_TIMEOUT_MS = 20_000L }
}
