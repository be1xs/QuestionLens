package com.photosolve.app.ui.solve

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

sealed interface SolveUiState {
    data object Loading : SolveUiState
    data class Success(val content: String) : SolveUiState
    data class Error(val message: String) : SolveUiState
}

@Composable
fun SolveResultScreen(
    state: SolveUiState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    canFavorite: Boolean = true,
    backLabel: String = "返回图片",
) {
    when (state) {
        SolveUiState.Loading -> LoadingContent()
        is SolveUiState.Success -> SuccessContent(
            content = state.content,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onBack = onBack,
            canFavorite = canFavorite,
            backLabel = backLabel,
        )
        is SolveUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = onRetry,
            onBack = onBack,
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = "正在分析题目…",
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SuccessContent(
    content: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    canFavorite: Boolean,
    backLabel: String,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("搜题结果", style = MaterialTheme.typography.headlineMedium)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
        ) {
            SelectionContainer {
                Text(text = content)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = { copyToClipboard(context, content) }) {
                Text("复制答案")
            }
            Button(onClick = onToggleFavorite, enabled = canFavorite) {
                Text(if (isFavorite) "取消收藏" else "收藏")
            }
            Button(onClick = onBack) {
                Text(backLabel)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("搜题失败", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 12.dp),
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onRetry) {
                Text("重试")
            }
            Button(onClick = onBack) {
                Text("返回图片")
            }
        }
    }
}

private fun copyToClipboard(context: Context, content: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("搜题结果", content))
}
