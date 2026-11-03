package com.photosolve.app.ui.input

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.photosolve.app.data.image.QuestionImages
import com.photosolve.app.domain.image.CropRect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ImageCropScreen(imageUri: Uri, onCropped: (Uri) -> Unit, onUseOriginal: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var error by remember(imageUri) { mutableStateOf<String?>(null) }
    var loading by remember(imageUri) { mutableStateOf(true) }
    var saving by remember(imageUri) { mutableStateOf(false) }
    var bounds by rememberSaveable(imageUri.toString()) { mutableStateOf(listOf(0.08f, 0.08f, 0.92f, 0.92f)) }
    val crop = CropRect(bounds[0], bounds[1], bounds[2], bounds[3])
    fun update(area: CropRect) { bounds = listOf(area.left, area.top, area.right, area.bottom) }
    LaunchedEffect(imageUri) {
        try { bitmap = QuestionImages.load(context, imageUri) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { error = "无法打开图片，请返回重新选择。" }
        finally { loading = false }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("裁剪题目", style = MaterialTheme.typography.headlineSmall)
        Text("拖动四角或四边调整宽高，拖动框内移动选区。")
        Box(Modifier.weight(1f).fillMaxWidth()) {
            bitmap?.let { CropCanvas(it, crop, { area -> if (!saving) update(area) }, Modifier.fillMaxSize()) }
            if (loading) CircularProgressIndicator()
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { update(CropRect.INITIAL) }, enabled = !saving) { Text("重置选区") }
            TextButton(onClick = { update(CropRect.FULL) }, enabled = !saving) { Text("全选") }
            TextButton(onClick = onUseOriginal, enabled = !saving && bitmap != null) { Text("使用原图") }
        }
        Button(enabled = bitmap != null && !saving, modifier = Modifier.fillMaxWidth(), onClick = {
            val source = bitmap ?: return@Button
            saving = true
            error = null
            scope.launch {
                try { onCropped(QuestionImages.crop(context, source, crop)) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (failure: Exception) { error = "裁剪保存失败，请重试或使用原图。" }
                finally { saving = false }
            }
        }) { Text(if (saving) "正在保存…" else "确认裁剪") }
    }
}
