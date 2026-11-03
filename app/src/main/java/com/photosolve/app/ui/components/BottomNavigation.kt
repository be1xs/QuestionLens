package com.photosolve.app.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.photosolve.app.ui.navigation.AppDestination

@Composable
fun BottomNavigation(selected: AppDestination, onSelect: (AppDestination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp, windowInsets = WindowInsets(0, 0, 0, 0)) {
        listOf(AppDestination.HOME to "首页", AppDestination.MY to "我的").forEach { (route, label) ->
            NavigationBarItem(selected = selected == route, onClick = { onSelect(route) },
                icon = { AppSymbol(if (route == AppDestination.HOME) Symbol.HOME else Symbol.PERSONAL) },
                label = { Text(label) })
        }
    }
}
