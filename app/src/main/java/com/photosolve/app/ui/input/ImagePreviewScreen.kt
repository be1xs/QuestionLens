package com.photosolve.app.ui.input

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.photosolve.app.data.image.QuestionImages
import com.photosolve.app.data.settings.DEFAULT_PROMPT
import kotlinx.coroutines.CancellationException

@Composable
fun ImagePreviewScreen(
    imageUri: Uri, prompt: String, onPrompt: (String) -> Unit,
    onSolve: () -> Unit, onChooseAgain: () -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var error by remember(imageUri) { mutableStateOf<String?>(null) }
    LaunchedEffect(imageUri) {
        try { bitmap = QuestionImages.load(context, imageUri) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { error = "图片无法读取，请重新选择。" }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("搜题", style = MaterialTheme.typography.headlineMedium)
        bitmap?.let {
            Image(it.asImageBitmap(), "题目图片", Modifier.fillMaxWidth().height(260.dp))
        } ?: Text(error ?: "正在加载图片…")
        OutlinedTextField(value = prompt, onValueChange = onPrompt,
            label = { Text("本题提示词") }, minLines = 3, maxLines = 6,
            supportingText = { Text("仅用于当前题目，新题自动恢复默认。") },
            modifier = Modifier.fillMaxWidth())
        TextButton(onClick = { onPrompt(DEFAULT_PROMPT) }) { Text("恢复默认提示词") }
        Button(onClick = onSolve, enabled = bitmap != null, modifier = Modifier.fillMaxWidth()) { Text("开始搜题") }
        OutlinedButton(onClick = onChooseAgain, modifier = Modifier.fillMaxWidth()) { Text("重新选择图片") }
    }
}
