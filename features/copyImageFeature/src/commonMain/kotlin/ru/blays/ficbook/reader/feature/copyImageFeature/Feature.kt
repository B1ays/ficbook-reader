package ru.blays.ficbook.reader.feature.copyImageFeature

import coil3.Image
import coil3.annotation.ExperimentalCoilApi

@OptIn(ExperimentalCoilApi::class)
public expect suspend fun copyImageToClipboard(image: Image): Boolean