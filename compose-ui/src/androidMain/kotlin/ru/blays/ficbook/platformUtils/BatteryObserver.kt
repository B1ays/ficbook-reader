package ru.blays.ficbook.platformUtils

import android.os.BatteryManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun rememberBatteryObserver(): State<Int> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val batteryLevel = remember {
        mutableIntStateOf(0)
    }

    DisposableEffect(Unit) {
        val batteryManager = context.getSystemService<BatteryManager>()

        if(batteryManager != null) {
            scope.launch {
                while(scope.isActive) {
                    batteryLevel.intValue = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    delay(5.seconds)
                }
            }
        }

        onDispose {
            scope.cancel()
        }
    }
    return batteryLevel
}