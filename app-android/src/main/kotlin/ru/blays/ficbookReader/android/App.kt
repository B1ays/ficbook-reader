package ru.blays.ficbookReader.android

import android.app.Application
import android.content.Intent
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.blays.ficbook.feature.fileDownload.downloadFeatureModule
import ru.blays.ficbookReader.platformUtils.createImageLoader
import ru.blays.ficbookReader.shared.di.sharedModule
import java.io.PrintWriter
import java.io.StringWriter

class App: Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            val stringWriter = StringWriter()
            exception.printStackTrace(PrintWriter(stringWriter))
            val stackTrace = stringWriter.toString()
            val intent = Intent(this, CrashHandlerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("StackTrace", stackTrace)
            }
            startActivity(intent)
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