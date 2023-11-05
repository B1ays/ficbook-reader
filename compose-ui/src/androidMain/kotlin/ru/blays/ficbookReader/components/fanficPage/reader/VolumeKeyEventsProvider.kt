package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.runtime.compositionLocalOf

val LocalVolumeKeysEventSource = compositionLocalOf {
    TwoWayVolumeKeysEventAdapter()
}

const val VOLUME_UP = 24
const val VOLUME_DOWN = 25
const val NO_EVENT = -1

class TwoWayVolumeKeysEventAdapter {
    private var lastEvent: Int = NO_EVENT
    private val collectors: MutableList<(key: Int) -> Boolean> = mutableListOf()

    fun onEvent(event: Int): Boolean {
        lastEvent = event
        return collectors.fold(false) { initial, new ->
            initial || new(event)
        }
    }

    fun collect(collector: (key: Int) -> Boolean) {
        collectors += collector
    }

    fun removeCollector(collector: (key: Int) -> Boolean) {
        collectors -= collector
    }

    fun dispose() {
        collectors.clear()
        lastEvent = NO_EVENT
    }
}