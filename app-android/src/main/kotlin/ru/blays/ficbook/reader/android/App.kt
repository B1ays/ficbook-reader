package ru.blays.ficbook.reader.android

import android.app.Application
import android.content.Intent
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.blays.ficbook.platformUtils.createImageLoader
import ru.blays.ficbook.reader.feature.fileDownload.downloadFeatureModule
import ru.blays.ficbook.reader.shared.di.sharedModule
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class App: Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            val stackTrace = StringWriter().apply {
                exception.printStackTrace(PrintWriter(this))
            }.toString()

            val intent = Intent(this, CrashHandlerActivity::class.java).apply {
                addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
                )
                putExtra("StackTrace", stackTrace)
            }
            startActivity(intent)
            exitProcess(0)
        }

        startKoin {
            androidContext(this@App)
            modules(
                sharedModule,
                downloadFeatureModule
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return createImageLoader(context, true)
    }
}