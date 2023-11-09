package ru.blays.ficbookReader.platformUtils

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun Modifier.blurPlatform(
    areas: Array<Rect>,
    backgroundColor: Color,
    tint: Color,
    blurRadius: Dp,
    noiseFactor: Float,
    edgeTreatment: BlurredEdgeTreatment
): Modifier {
    val clip: Boolean
    val tileMode: TileMode
    if (edgeTreatment.shape != null) {
        clip = true
        tileMode = TileMode.Clamp
    } else {
        clip = false
        tileMode = TileMode.Decal
    }

    return if ((blurRadius > 0.dp && blurRadius > 0.dp) || clip) {
        val tintColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tint
        } else {
            tint.copy(
                alpha = (tint.alpha * 1.35F).coerceAtMost(1F)
            )
        }
        graphicsLayer {
            val horizontalBlurPixels = blurRadius.toPx()
            val verticalBlurPixels = blurRadius.toPx()

            this.renderEffect =
                if (horizontalBlurPixels > 0f && verticalBlurPixels > 0f) {
                    BlurEffect(horizontalBlurPixels, verticalBlurPixels, tileMode)
                } else {
                    null
                }
            this.shape = edgeTreatment.shape ?: RectangleShape
            this.clip = clip
        } then drawWithCache {
            onDrawWithContent {
                drawContent()
                areas.forEach {
                    drawRect(
                        color = tintColor,
                        size = Size(it.width, it.height),
                    )
                }
            }
        }
    } else {
        this
    }
}

actual val blurSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S