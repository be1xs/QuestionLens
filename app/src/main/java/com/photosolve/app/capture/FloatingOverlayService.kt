package com.photosolve.app.capture

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class FloatingOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var window: WindowManager? = null
    private var button: FloatingOrbView? = null
    private val permissionListener = AppOpsManager.OnOpChangedListener { operation, packageName ->
        if (operation == AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW && packageName == this.packageName) {
            handler.post { if (!Settings.canDrawOverlays(this)) stopSelf() }
        }
    }
    private var capturePending = false
    private val restore = Runnable { capturePending = false; button?.visibility = View.VISIBLE }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        getSystemService(AppOpsManager::class.java).startWatchingMode(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, packageName, permissionListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) { stopSelf(); return START_NOT_STICKY }
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "悬浮窗识屏", NotificationManager.IMPORTANCE_LOW))
            val notification = notification()
            if (Build.VERSION.SDK_INT >= 34) startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else startForeground(ID, notification)
            showButton()
            OverlayState.update(OverlayStatus.ON)
            when (intent?.action) {
                ACTION_HIDE_OVERLAY -> hide()
                ACTION_RESTORE_OVERLAY -> { handler.removeCallbacks(restore); restore.run() }
            }
        } catch (error: Exception) {
            Toast.makeText(this, "悬浮窗启动失败，请检查悬浮窗和后台运行权限。", Toast.LENGTH_LONG).show()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showButton() {
        if (button != null) return
        window = getSystemService(WindowManager::class.java)
        val params = WindowManager.LayoutParams(
            dp(52), dp(52), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.START; x = dp(24); y = dp(180) }
        val view = FloatingOrbView(this).apply {
            setOnClickListener { beginCapture() }
        }
        var downX = 0f; var downY = 0f
        var originX = 0; var originY = 0; var dragging = false
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    downX = event.rawX; downY = event.rawY
                    originX = params.x; originY = params.y; dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (abs(dx) > slop || abs(dy) > slop) dragging = true
                    if (dragging) {
                        view.isPressed = false
                        val metrics = resources.displayMetrics
                        params.x = (originX + dx.toInt()).coerceIn(0, (metrics.widthPixels - params.width).coerceAtLeast(0))
                        params.y = (originY + dy.toInt()).coerceIn(0, (metrics.heightPixels - params.height).coerceAtLeast(0))
                        try { window?.updateViewLayout(view, params) }
                        catch (error: IllegalArgumentException) { stopSelf() }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    if (!dragging) view.performClick()
                }
                MotionEvent.ACTION_CANCEL -> view.isPressed = false
            }
            true
        }
        window?.addView(view, params)
        button = view
    }

    private fun hide() {
        capturePending = true
        button?.visibility = View.INVISIBLE
        handler.removeCallbacks(restore)
        handler.postDelayed(restore, RESTORE_TIMEOUT_MS)
    }

    private fun beginCapture() {
        if (capturePending) return
        // Keep the overlay visible until startActivity is issued (Android 15+ BAL rule).
        try {
            startActivity(captureIntent())
            hide()
        } catch (error: Exception) {
            Toast.makeText(this, "请从通知栏点击识屏，或允许后台弹出界面权限。", Toast.LENGTH_LONG).show()
        }
    }

    private fun captureIntent() = Intent(this, ScreenCapturePermissionActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    private fun notification(): Notification {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val capture = PendingIntent.getActivity(this, ID, captureIntent(), flags)
        val stop = PendingIntent.getService(this, ID + 1,
            Intent(this, FloatingOverlayService::class.java).setAction(ACTION_STOP_OVERLAY), flags)
        return NotificationCompat.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("拍题助手识屏已开启").setContentText("点击识屏，或关闭悬浮窗")
            .setContentIntent(capture).setOngoing(true)
            .addAction(android.R.drawable.ic_menu_camera, "识屏", capture)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", stop).build()
    }

    override fun onDestroy() {
        OverlayState.update(OverlayStatus.OFF)
        getSystemService(AppOpsManager::class.java).stopWatchingMode(permissionListener)
        handler.removeCallbacksAndMessages(null)
        button?.let { if (it.isAttachedToWindow) window?.removeView(it) }
        button = null
        super.onDestroy()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private companion object {
        const val CHANNEL = "floating_overlay"
        const val ID = 1001
        const val RESTORE_TIMEOUT_MS = 90_000L
    }
}
