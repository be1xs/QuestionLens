package com.photosolve.app.ui.solve

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.photosolve.app.data.history.HistoryRepository
import com.photosolve.app.data.network.ApiException
import com.photosolve.app.data.settings.ApiSettingsRepository
import com.photosolve.app.data.settings.DEFAULT_PROMPT
import com.photosolve.app.domain.solve.SolveQuestionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolveController(
    private val scope: CoroutineScope,
    private val settings: ApiSettingsRepository,
    private val history: HistoryRepository,
    private val solve: SolveQuestionUseCase,
) {
    var state by mutableStateOf<SolveUiState>(SolveUiState.Loading)
        private set
    var recordId by mutableStateOf<Long?>(null)
        private set
    var isFavorite by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    private var request: Job? = null
    private var favoriteJob: Job? = null

    fun cancel() {
        request?.cancel()
    }

    fun start(uri: Uri, prompt: String) {
        cancel()
        favoriteJob?.cancel()
        recordId = null
        isFavorite = false
        message = null
        state = SolveUiState.Loading
        request = scope.launch {
            try {
                val configuration = settings.settings.first().copy(
                    prompt = prompt.ifBlank { DEFAULT_PROMPT },
                )
                val result = solve(uri, configuration)
                ensureActive()
                state = SolveUiState.Success(result.content)
                try {
                    recordId = withContext(Dispatchers.IO) { history.save(uri, result.content) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    message = "答案已生成，但历史保存失败，请先复制答案。"
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                state = SolveUiState.Error(
                    if (error is ApiException) error.message.orEmpty() else "搜题失败，请检查配置后重试",
                )
            }
        }
    }

    fun toggleFavorite() {
        val id = recordId ?: return
        if (favoriteJob?.isActive == true) return
        val next = !isFavorite
        favoriteJob = scope.launch {
            try {
                history.setFavorite(id, next)
                isFavorite = next
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                message = "收藏更新失败，请重试"
            }
        }
    }
}
