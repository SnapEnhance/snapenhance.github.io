package me.rhunk.snapenhance.ui.util.coil

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.SourceResult
import me.rhunk.snapenhance.common.data.FileType
import me.rhunk.snapenhance.common.data.download.MediaEncryptionKeyPair
import me.rhunk.snapenhance.common.data.download.SplitMediaAssetType
import me.rhunk.snapenhance.common.logger.AbstractLogger
import me.rhunk.snapenhance.common.util.snap.MediaDownloaderHelper
import me.rhunk.snapenhance.core.util.media.PreviewUtils

class CoilPreviewDecoder(
    private val resources: Resources,
    private val sourceResult: SourceResult,
    private val encryptionKeyPair: MediaEncryptionKeyPair? = null,
    private val mergeOverlay: Boolean = false
): Decoder {
    override suspend fun decode(): DecodeResult {
        return sourceResult.source.file().toFile().inputStream().use { fileInputStream ->
            val cipherInputStream = encryptionKeyPair?.decryptInputStream(fileInputStream) ?: fileInputStream

            var bitmap: Bitmap? = null
            var overlayBitmap: Bitmap? = null

            MediaDownloaderHelper.getSplitElements(cipherInputStream) { type, inputStream ->
                if (inputStream.available() > 50 * 1024 * 1024) {
                    return@getSplitElements
                }
                if (type == SplitMediaAssetType.ORIGINAL || (mergeOverlay && type == SplitMediaAssetType.OVERLAY)) {
                    runCatching {
                        val bytes = inputStream.readBytes()
                        PreviewUtils.createPreview(bytes, isVideo = FileType.fromByteArray(bytes).isVideo)?.let {
                            if (type == SplitMediaAssetType.ORIGINAL) {
                                bitmap = it
                            } else {
                                overlayBitmap = it
                            }
                        }
                    }.onFailure {
                        AbstractLogger.directError("CoilPreviewDecoder", it)
                    }
                }
            }

            if (mergeOverlay && overlayBitmap != null) {
                bitmap = PreviewUtils.mergeBitmapOverlay(bitmap!!, overlayBitmap!!)
            }

            cipherInputStream.close()

            DecodeResult(
                drawable = BitmapDrawable(resources, bitmap!!),
                isSampled = true
            )
        }
    }
}