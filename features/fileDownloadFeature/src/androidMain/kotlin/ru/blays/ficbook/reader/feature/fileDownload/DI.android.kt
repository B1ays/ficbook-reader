package ru.blays.ficbook.reader.feature.fileDownload

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val downloadFeatureModule: Module = module {
    singleOf(::NotificationUtils)
}