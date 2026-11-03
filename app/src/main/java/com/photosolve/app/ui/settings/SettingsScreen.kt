package com.photosolve.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.photosolve.app.data.settings.ApiSettings
import com.photosolve.app.data.settings.ApiSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: ApiSettingsRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf<ApiSettings?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saveCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(repository) {
        settings = repository.settings.first()
    }

    val currentSettings = settings
    if (currentSettings == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("API 设置", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = currentSettings.baseUrl,
            onValueChange = { settings = currentSettings.copy(baseUrl = it) },
            label = { Text("API Base URL") },
            supportingText = { Text("例如 https://api.openai.com/v1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = currentSettings.apiKey,
            onValueChange = { settings = currentSettings.copy(apiKey = it) },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = currentSettings.modelName,
            onValueChange = { settings = currentSettings.copy(modelName = it) },
            label = { Text("模型名称") },
            supportingText = { Text("例如 gpt-4o-mini 或 deepseek-vl2") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("只显示答案", style = MaterialTheme.typography.titleMedium)
                Text("关闭时会显示默认解析", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = currentSettings.answerOnly,
                onCheckedChange = { settings = currentSettings.copy(answerOnly = it) },
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (saveCompleted) {
            Text("设置已保存", color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBack) {
                Text("返回")
            }
            Button(onClick = {
                scope.launch {
                    runCatching { repository.save(currentSettings) }
                        .onSuccess {
                            errorMessage = null
                            saveCompleted = true
                        }
                        .onFailure {
                            saveCompleted = false
                            errorMessage = "保存失败，请稍后重试"
                        }
                }
            }) {
                Text("保存设置")
            }
        }
    }
}
