package ru.blays.ficbook.reader.shared.platformUtils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.russhwolf.settings.get
import org.koin.java.KoinJavaComponent
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings

actual fun openInBrowser(url: String) {
    val context: Context by KoinJavaComponent.getKoin().inject()
    val chromeCustomTabs = settings.get(
        key = SettingsKeys.CHROME_CUSTOM_TABS_KEY,
        defaultValue = false
    )
    val chromeInstalled = context.isPackageInstalled(CHROME_PACKAGE_NAME)

    try {
        val uri = url.toUri()

        if(chromeCustomTabs && chromeInstalled) {
            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            builder.setInstantAppsEnabled(false)
            val customTabsIntent = builder.build()

            customTabsIntent.intent.apply {
                setPackage(CHROME_PACKAGE_NAME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            customTabsIntent.launchUrl(context, uri)
            return
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
    val clipboard = context.getSystemService<ClipboardManager>()

    if(clipboard == null) {
        Toast.makeText(
            context,
            "ClipboardManager не найден",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            "Скопировано в буфер обмена",
            Toast.LENGTH_SHORT
        ).show()
    }
}

actual fun shareText(text: String) {
    val context: Context by KoinJavaComponent.getKoin().inject()
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    val chooserIntent = Intent.createChooser(intent, "Поделиться")
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooserIntent)
}

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

private const val CHROME_PACKAGE_NAME = "com.android.chrome"