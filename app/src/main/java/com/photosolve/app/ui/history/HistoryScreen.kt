package com.photosolve.app.ui.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.photosolve.app.data.history.HistoryEntity
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    records: List<HistoryEntity>,
    onSelect: (HistoryEntity) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onBack: () -> Unit,
    favoritesOnly: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (favoritesOnly) "我的收藏" else "历史记录",
                style = MaterialTheme.typography.headlineMedium,
            )
            Button(onClick = onBack) { Text("返回") }
        }
        if (records.isEmpty()) {
            Text(
                text = if (favoritesOnly) "还没有收藏" else "还没有搜题记录",
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryItem(record, onSelect, onDelete)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    record: HistoryEntity,
    onSelect: (HistoryEntity) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
) {
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = record.imagePath) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                File(record.imagePath).inputStream().use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(record) },
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "历史题目图片",
                    modifier = Modifier.height(80.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = DateFormat.getDateTimeInstance().format(Date(record.createdAt)),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = record.answerContent.take(PREVIEW_LENGTH),
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(if (record.isFavorite) "已收藏" else "", color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = { onDelete(record) }) { Text("删除") }
        }
    }
}

private const val PREVIEW_LENGTH = 80
