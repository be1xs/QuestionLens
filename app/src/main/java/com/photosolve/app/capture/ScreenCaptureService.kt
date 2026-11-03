package com.photosolve.app.capture

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean

class ScreenCaptureService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private val worker = HandlerThread("question-screen-capture")
    private lateinit var background: Handler
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private val completed = AtomicBoolean(false)
    private var started = false
    private var readyAt = 0L
    private val timeout = Runnable { finishCapture(error = "未收到屏幕画面，请解锁屏幕后重试。") }
    private val callback = object : MediaProjection.Callback() {
        override fun onStop() { finishCapture(error = "屏幕共享已停止，请重新授权。") }
    }

    override fun onCreate() {
        super.onCreate()
        worker.start()
        background = Handler(worker.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (started) return START_NOT_STICKY
        started = true
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "屏幕截图", NotificationManager.IMPORTANCE_LOW))
            startForeground(ID, NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_camera).setContentTitle("正在截取题目")
                .setContentText("截图完成后进入裁剪").setOngoing(true).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            val data = intent?.let { authorizationData(it) }
            if (resultCode != Activity.RESULT_OK || data == null) {
                finishCapture(error = "没有有效的截图授权，请重新点击识屏。")
            } else capture(resultCode, data)
        } catch (error: Exception) {
            finishCapture(error = "截图启动失败，请重新授权并检查系统后台运行权限。")
        }
        return START_NOT_STICKY
    }

    private fun capture(resultCode: Int, data: Intent) {
        val metrics = resources.displayMetrics
        val bounds = if (Build.VERSION.SDK_INT >= 30) getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds else null
        val width = bounds?.width() ?: metrics.widthPixels
        val height = bounds?.height() ?: metrics.heightPixels
        val session = getSystemService(MediaProjectionManager::class.java).getMediaProjection(resultCode, data)
        projection = session
        // Required before creating the virtual display on Android 14+.
        session.registerCallback(callback, main)
        val images = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, MAX_IMAGES)
        reader = images
        readyAt = SystemClock.uptimeMillis() + SETTLE_MS
        images.setOnImageAvailableListener({ source ->
            if (!completed.get() && SystemClock.uptimeMillis() >= readyAt) readFrame(source)
        }, background)
        display = session.createVirtualDisplay("PhotoSolveCapture", width, height, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, images.surface, null, main)
        // Also consume a buffered frame when the screen is static and no new frame arrives.
        background.postDelayed({ if (!completed.get()) readFrame(images) }, SETTLE_MS + 100)
        main.postDelayed(timeout, TIMEOUT_MS)
    }

    private fun readFrame(source: ImageReader) {
        try {
            val frame = source.acquireLatestImage() ?: return
            frame.use {
                if (completed.compareAndSet(false, true)) {
                    try {
                        val uri = ScreenImageWriter.save(this, it)
                        main.post { deliver(uri.toString(), null) }
                    } catch (error: Exception) {
                        main.post { deliver(null, "截图保存失败，请检查可用存储空间后重试。") }
                    }
                }
            }
        } catch (error: Exception) {
            finishCapture(error = "无法读取屏幕画面，请重新授权。")
        }
    }

    private fun finishCapture(error: String) {
        if (completed.compareAndSet(false, true)) main.post { deliver(null, error) }
    }

    private fun deliver(uri: String?, error: String?) {
        main.removeCallbacks(timeout)
        sendBroadcast(Intent(ACTION_CAPTURE_FINISHED).setPackage(packageName)
            .putExtra(EXTRA_SCREENSHOT_URI, uri).putExtra(EXTRA_CAPTURE_ERROR, error))
        stopSelf()
    }

    override fun onDestroy() {
        completed.set(true)
        main.removeCallbacksAndMessages(null)
        projection?.unregisterCallback(callback)
        display?.release()
        projection?.stop()
        background.post { reader?.close(); worker.quitSafely() }
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("DEPRECATION")
    private fun authorizationData(intent: Intent): Intent? = if (Build.VERSION.SDK_INT >= 33) {
        intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
    } else intent.getParcelableExtra(EXTRA_RESULT_DATA)

    private companion object {
        const val CHANNEL = "screen_capture"
        const val ID = 1003
        const val MAX_IMAGES = 2
        const val SETTLE_MS = 600L
        const val TIMEOUT_MS = 12_000L
    }
}
