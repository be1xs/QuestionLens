package com.photosolve.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.photosolve.app.capture.OverlayStatus
import com.photosolve.app.ui.components.AppSymbol
import com.photosolve.app.ui.components.Symbol

@Composable
fun HomeScreen(
    onStart: () -> Unit,
    overlayStatus: OverlayStatus,
    onToggleOverlay: () -> Unit,
) {
    val compact = LocalConfiguration.current.screenHeightDp < 480
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 24.dp),
    ) {
        Text("拍题助手", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!compact) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("每一道题，\n都有思路。", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(if (compact) 16.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 20.dp)) {
                if (!compact) Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    AppSymbol(Symbol.SCAN, Modifier.padding(16.dp).size(32.dp), MaterialTheme.colorScheme.primary)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("拍照或从相册选择，自由框选题目。", style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.medium) {
                    Text("开始搜题", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    AppSymbol(Symbol.ARROW)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppSymbol(Symbol.ORB, color = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("悬浮识屏", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(when (overlayStatus) {
                            OverlayStatus.ON -> "已开启 · 点击圆球即可识屏"
                            OverlayStatus.STARTING -> "正在开启…"
                            OverlayStatus.OFF -> "未开启"
                        }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = overlayStatus == OverlayStatus.ON,
                        onCheckedChange = { onToggleOverlay() }, enabled = overlayStatus != OverlayStatus.STARTING,
                        modifier = Modifier.semantics { contentDescription = "悬浮识屏开关" })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("在其他应用看题时，轻点圆球截取当前屏幕。", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
