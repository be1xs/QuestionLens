package com.photosolve.app.ui.navigation

enum class AppDestination {
    HOME, MY, SETTINGS, SOURCE, CROP, PREVIEW, RESULT, HISTORY, HISTORY_DETAIL;

    fun parent(cropFromSource: Boolean = true): AppDestination = when (this) {
        HOME, MY, SOURCE -> HOME
        SETTINGS, HISTORY -> MY
        CROP -> if (cropFromSource) SOURCE else HOME
        PREVIEW -> CROP
        RESULT -> PREVIEW
        HISTORY_DETAIL -> HISTORY
    }
}
