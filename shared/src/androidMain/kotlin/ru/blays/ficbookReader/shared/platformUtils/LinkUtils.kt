package ru.blays.ficbookReader.shared.platformUtils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.koin.java.KoinJavaComponent

actual fun openUrl(url: String) {
    val context: Context by KoinJavaComponent.getKoin().inject()
    val intent = Intent(
        Intent.ACTION_VIEW,
        url.toUri()
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}