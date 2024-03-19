package ru.blays.ficbook.reader.shared.platformUtils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.russhwolf.settings.get
import org.koin.java.KoinJavaComponent
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.FICBOOK_HOST
import ru.blays.ficbook.reader.shared.data.chromePackages
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings
import ru.blays.ficbook.reader.shared.utils.firstInstalledPackage

/**
 * Open the given URL in a browser
 *
 * @param url The URL to be opened
 */
actual fun openInBrowser(url: String) {
    val context: Context by getKoin().inject()
    val chromeCustomTabs = settings.get(
        key = SettingsKeys.CHROME_CUSTOM_TABS_KEY,
        defaultValue = false
    )

    val chromePackageName = context.firstInstalledPackage(chromePackages)

    try {
        val uri = url.toUri()

        if (chromeCustomTabs && chromePackageName != null) {
            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            builder.setInstantAppsEnabled(false)
            val customTabsIntent = builder.build()

            customTabsIntent.intent.apply {
                setPackage(chromePackageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            customTabsIntent.launchUrl(context, uri)
            return
        }

        if (uri.isFicbookUri) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val emptyBrowserIntent = Intent().apply {
                    setAction(Intent.ACTION_VIEW)
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    setData(Uri.fromParts("http", "", null))
                }
                val targetIntent = Intent().apply {
                    setAction(Intent.ACTION_VIEW)
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    setData(uri)
                    setSelector(emptyBrowserIntent)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(targetIntent)
            } else {
                val defaultBrowser = Intent.makeMainSelectorActivity(
                    Intent.ACTION_MAIN,
                    Intent.CATEGORY_APP_BROWSER
                ).apply {
                    setData(uri)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(defaultBrowser)
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Не удаётся открыть ссылку",
            Toast.LENGTH_SHORT
        ).show()
    }
}

actual fun copyToClipboard(text: String) {
    val context: Context by KoinJavaComponent.getKoin().inject()
    val clipboard = context.getSystemService<ClipboardManager>() ?: return

    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(
            context,
            "Скопировано в буфер обмена",
            Toast.LENGTH_SHORT
        ).show()
    }
}

actual fun shareText(text: String) {
    val context: Context by KoinJavaComponent.getKoin().inject()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooserIntent = Intent.createChooser(intent, "Поделиться").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooserIntent)
}

private val Uri.isFicbookUri
    get() = host == FICBOOK_HOST