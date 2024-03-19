package ru.blays.ficbook.reader.shared.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

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

fun Context.isAnyPackageInstalled(packageNames: Array<String>): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageNames.forEach { packageName ->
            try {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                return true
            } catch (_: PackageManager.NameNotFoundException) {}
        }
        return false
    } else {
        try {
            packageManager.getPackageInfo(packageName, 0)
            return true
        } catch (_: PackageManager.NameNotFoundException) {}
        return false
    }
}

fun Context.firstInstalledPackage(packageNames: Array<String>): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageNames.forEach { packageName ->
            try {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                return packageName
            } catch (e: PackageManager.NameNotFoundException) {
                return null
            }
        }
        return null
    } else {
        try {
            packageManager.getPackageInfo(packageName, 0)
            return packageName
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
    }
}