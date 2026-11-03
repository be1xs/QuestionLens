package com.photosolve.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.photosolve.app.capture.FloatingOverlayService
import com.photosolve.app.capture.OverlayState
import com.photosolve.app.capture.OverlayStatus
import com.photosolve.app.ui.components.AppSymbol
import com.photosolve.app.ui.components.Symbol
import com.photosolve.app.ui.components.BottomNavigation
import com.photosolve.app.ui.theme.PhotoSolveTheme
import com.photosolve.app.data.history.HistoryEntity
import com.photosolve.app.data.history.HistoryRepository
import com.photosolve.app.data.settings.ApiSettingsRepository
import com.photosolve.app.data.settings.DEFAULT_PROMPT
import com.photosolve.app.domain.solve.SolveQuestionUseCase
import com.photosolve.app.ui.history.HistoryScreen
import com.photosolve.app.ui.input.ImageCropScreen
import com.photosolve.app.ui.input.ImagePreviewScreen
import com.photosolve.app.ui.input.PhotoSourceScreen
import com.photosolve.app.ui.navigation.AppDestination
import com.photosolve.app.ui.navigation.AppDestination.*
import com.photosolve.app.ui.settings.SettingsScreen
import com.photosolve.app.ui.solve.SolveController
import com.photosolve.app.ui.solve.SolveResultScreen
import com.photosolve.app.ui.solve.SolveUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun PhotoSolveApp(
    settings: ApiSettingsRepository,
    history: HistoryRepository,
    solve: SolveQuestionUseCase,
    screenshot: Uri?,
    onScreenshotConsumed: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { SolveController(scope, settings, history, solve) }
    var route by rememberSaveable { mutableStateOf(HOME) }
    var source by rememberSaveable { mutableStateOf<String?>(null) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var prompt by rememberSaveable { mutableStateOf(DEFAULT_PROMPT) }
    var cropFromSource by rememberSaveable { mutableStateOf(true) }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var historyId by rememberSaveable { mutableStateOf<Long?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val records by remember(history) { history.observeAll() }.collectAsState(initial = emptyList())
    val record = records.firstOrNull { it.id == historyId }
    val startOverlay = rememberStartOverlay { message = it }
    val overlayStatus by OverlayState.status.collectAsState()
    val isRoot = route == HOME || route == MY
    val back = {
        if (route == RESULT) controller.cancel()
        route = route.parent(cropFromSource)
    }
    BackHandler(enabled = route != HOME, onBack = back)
    DisposableEffect(controller) { onDispose { controller.cancel() } }
    LaunchedEffect(screenshot) {
        screenshot?.let {
            controller.cancel()
            source = it.toString()
            selected = null
            prompt = DEFAULT_PROMPT
            cropFromSource = false
            route = CROP
            onScreenshotConsumed()
        }
    }
    fun startRequest() {
        selected?.let { controller.start(Uri.parse(it), prompt); route = RESULT }
    }
    fun updateRecord(action: suspend () -> Unit) {
        scope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { message = "记录更新失败，请重试" }
        }
    }

    PhotoSolveTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                if (!isRoot) {
                    TextButton(onClick = back) {
                        AppSymbol(Symbol.BACK)
                        Text("返回", Modifier.padding(start = 8.dp))
                    }
                }
                message?.let { text ->
                    Text(text, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { message = null }) { Text("知道了") }
                }
                Box(Modifier.weight(1f)) {
                    when (route) {
                        HOME -> HomeScreen(
                            onStart = { route = SOURCE },
                            overlayStatus = overlayStatus,
                            onToggleOverlay = {
                                if (overlayStatus == OverlayStatus.ON) {
                                    context.stopService(Intent(context, FloatingOverlayService::class.java))
                                } else startOverlay()
                            },
                        )
                        MY -> MyScreen(records.size, records.count { it.isFavorite },
                            onSettings = { route = SETTINGS },
                            onHistory = { favoritesOnly = it; route = HISTORY })
                        SETTINGS -> SettingsScreen(settings, back)
                        SOURCE -> PhotoSourceScreen(
                            onImageReady = {
                                source = it.toString(); selected = null
                                prompt = DEFAULT_PROMPT; cropFromSource = true; route = CROP
                            }, onBack = back,
                        )
                        CROP -> source?.let { image ->
                            ImageCropScreen(Uri.parse(image),
                                onCropped = { selected = it.toString(); route = PREVIEW },
                                onUseOriginal = { selected = image; route = PREVIEW }, onBack = back)
                        }
                        PREVIEW -> selected?.let { image ->
                            ImagePreviewScreen(Uri.parse(image), prompt, { prompt = it },
                                onSolve = ::startRequest,
                                onChooseAgain = { route = SOURCE })
                        }
                        RESULT -> Column {
                            controller.message?.let { Text(it, Modifier.padding(16.dp)) }
                            SolveResultScreen(controller.state, controller.isFavorite,
                                controller::toggleFavorite, ::startRequest, back,
                                canFavorite = controller.recordId != null)
                        }
                        HISTORY -> HistoryScreen(
                            records = if (favoritesOnly) records.filter(HistoryEntity::isFavorite) else records,
                            onSelect = { historyId = it.id; route = HISTORY_DETAIL },
                            onDelete = { updateRecord { history.delete(it) } },
                            onBack = back, favoritesOnly = favoritesOnly,
                        )
                        HISTORY_DETAIL -> if (record != null) {
                            SolveResultScreen(SolveUiState.Success(record.answerContent), record.isFavorite,
                                { updateRecord { history.setFavorite(record.id, !record.isFavorite) } },
                                {}, back, backLabel = "返回列表")
                        } else Text("记录不存在或尚在加载，请返回列表重试。", Modifier.padding(24.dp))
                    }
                }
                if (isRoot) BottomNavigation(route) { route = it }
            }
        }
    }
}
