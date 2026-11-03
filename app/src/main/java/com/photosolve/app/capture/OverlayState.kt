package com.photosolve.app.capture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OverlayStatus { OFF, STARTING, ON }

/** Process-local state; a killed process cannot retain an active overlay window. */
object OverlayState {
    private val mutableStatus = MutableStateFlow(OverlayStatus.OFF)
    val status = mutableStatus.asStateFlow()
    internal fun update(status: OverlayStatus) { mutableStatus.value = status }
}
