package ru.blays.ficbook.ui_components.Test

import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.systemBarsPadding
import kotlinx.coroutines.delay
import ru.blays.ficbook.values.DefaultPadding
import kotlin.time.Duration.Companion.seconds

private const val itemsCount = 12

@Composable
actual fun CaptureTest() {
    val bitmapList = remember { MutableList<Bitmap?>(itemsCount) { null } }

    var draw by remember { mutableStateOf(false) }
    var showBitmaps by remember { mutableStateOf(false) }

    LaunchedEffect(draw) {
        if (draw) {
            delay(1.seconds)
            draw = false
        }
    }

    Column(
        modifier = Modifier.systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxWidth()
        ) {
            Text("Draw into bitmap")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = draw,
                onCheckedChange = { draw = it }
            )
        }
        Row(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxWidth()
        ) {
            Text("Show bitmaps")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = showBitmaps,
                onCheckedChange = { showBitmaps = it }
            )
        }
        Button(
            onClick = {
                Log.i("CaptureTest", "Bitmaps:${bitmapList.joinToString()}, count: ${bitmapList.size}")
            }
        ) {
            Text("Print bitmaps")
        }
        Button(
            onClick = {
                bitmapList.replaceAll { null }
            }
        ) {
            Text("Clear bitmaps")
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!showBitmaps) {
                items(itemsCount) {
                    TestItem(i = it)
                }
            } else {
                items(bitmapList) {
                    it?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = MaterialTheme.colorScheme.errorContainer,
                                blendMode = BlendMode.Overlay
                            )
                        )
                         Spacer(modifier = Modifier.height(6.dp),)
                    }
                }
            }
        }
    }
    IterateToBitmap(draw, bitmapList)
}

@Composable
fun TestItem(
    modifier: Modifier = Modifier,
    i: Int,
) {
    Card(
        modifier = modifier.padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .height(600.dp),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
        ) {
            Text("Item: $i")
        }
    }
}

@Composable
private fun IterateToBitmap(draw: Boolean, bitmaps: MutableList<Bitmap?>) {
    if(draw) {
        for (i in 0 until itemsCount) {
            TestItem(
                modifier = Modifier.capturePicture(
                    bitmapList = bitmaps,
                    drawIntoFile = draw,
                    index = i
                ),
                i = i
            )
        }
    }
}

fun Modifier.capturePicture(
    bitmapList: MutableList<Bitmap?>,
    drawIntoFile: Boolean,
    index: Int
): Modifier {
    return this then CaptureModifierElement(bitmapList, drawIntoFile, index)
}

class CaptureModifier(
    private val bitmapList: MutableList<Bitmap?>,
    var drawIntoFile: Boolean,
    var index: Int
): Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() {
        val width = size.width.toInt()
        val height = size.height.toInt()
        val picture = Picture()

        val pictureCanvas = Canvas(
            picture.beginRecording(
                width,
                height
            )
        )
        draw(this, layoutDirection, pictureCanvas, this.size) internalDraw@ {
            this@draw.drawContent()
        }
        picture.endRecording()

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Bitmap.createBitmap(picture)
        } else {
            val bitmap = Bitmap.createBitmap(
                picture.width,
                picture.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawPicture(picture)
            bitmap
        }
        try {
            bitmapList[index] = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class CaptureModifierElement(
    val bitmapList: MutableList<Bitmap?>,
    val drawIntoFile: Boolean,
    val index: Int
): ModifierNodeElement<CaptureModifier>() {
    override fun create(): CaptureModifier = CaptureModifier(bitmapList, drawIntoFile, index)

    override fun update(node: CaptureModifier) {
        node.drawIntoFile = drawIntoFile
        node.index = index
    }
}