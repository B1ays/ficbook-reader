package ru.blays.ficbookReader.platformUtils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
@Composable
fun rememberTimeObserver(
    timePattern: String = "HH:mm"
): State<String> {
    val context = LocalContext.current
    val timeFormat = remember {
        SimpleDateFormat(timePattern)
    }
    val time = remember {
        mutableStateOf(timeFormat.format(Date()))
    }
    DisposableEffect(key1 = Unit) {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action?.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    time.value = timeFormat.format(Date())
                }
            }
        }
        context.registerReceiver(
            broadcastReceiver,
            IntentFilter(Intent.ACTION_TIME_TICK)
        )
        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    return time
}