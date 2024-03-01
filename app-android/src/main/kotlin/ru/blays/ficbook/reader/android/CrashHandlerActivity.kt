package ru.blays.ficbook.reader.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import ru.blays.ficbook.reader.shared.platformUtils.copyToClipboard
import ru.blays.ficbook.reader.shared.platformUtils.shareText
import ru.blays.ficbook.ui_components.LogView.LogView
import java.time.LocalDate
import java.time.LocalTime

class CrashHandlerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stackTrace = intent.getStringExtra("StackTrace") ?: ""

        val fullLog = additionalInfo + stackTrace

        onBackPressedDispatcher.addCallback(this, true) {
            finishAndRemoveTask()
        }

        setContent {
            LogView(
                log = fullLog,
                actionCopy = ::copyToClipboard,
                actionShare = ::shareText
            )
        }
    }
}

private val additionalInfo: String get() =
    """date: ${LocalDate.now()} | ${LocalTime.now()}
android version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})
device model: ${Build.DEVICE}
device brand: ${Build.BRAND}
Supported abi: ${Build.SUPPORTED_ABIS.joinToString()}
============""".trimIndent() + "\n"