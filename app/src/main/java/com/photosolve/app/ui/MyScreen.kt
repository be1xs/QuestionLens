package com.photosolve.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.photosolve.app.ui.components.AppSymbol
import com.photosolve.app.ui.components.Symbol

@Composable
fun MyScreen(historyCount: Int, favoriteCount: Int, onSettings: () -> Unit, onHistory: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("我的", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column {
                MenuEntry(Symbol.API, "API 设置", "连接你自己的视觉模型", onClick = onSettings)
                HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                MenuEntry(Symbol.HISTORY, "历史记录", "$historyCount 条解题记录", onClick = { onHistory(false) })
                HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                MenuEntry(Symbol.BOOKMARK, "我的收藏", "$favoriteCount 道值得回看的题", onClick = { onHistory(true) })
            }
        }
        Text("记录与收藏保存在本机", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun MenuEntry(symbol: Symbol, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                AppSymbol(symbol, Modifier.padding(12.dp), MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AppSymbol(Symbol.ARROW, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
