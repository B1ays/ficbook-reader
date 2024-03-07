package ru.blays.ficbook.reader.feature.copyImageFeature

import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import org.jetbrains.skiko.toBufferedImage
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage

@OptIn(ExperimentalCoilApi::class)
actual suspend fun copyImageToClipboard(image: Image): Boolean {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    val bufferedImage = image.asBitmap().toBufferedImage()

    return try {
        val transferable = TransferableImage(bufferedImage)
        clipboard.setContents(transferable, null)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@JvmRecord
internal data class TransferableImage(val image: BufferedImage) : Transferable {
    @Throws(UnsupportedFlavorException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (flavor == DataFlavor.imageFlavor) {
            return image
        } else {
            throw UnsupportedFlavorException(flavor)
        }
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(DataFlavor.imageFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        val flavors = transferDataFlavors
        for (dataFlavor in flavors) {
            if (flavor == dataFlavor) {
                return true
            }
        }
        return false
    }
}