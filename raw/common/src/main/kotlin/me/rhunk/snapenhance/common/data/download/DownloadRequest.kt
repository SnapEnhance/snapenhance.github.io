package me.rhunk.snapenhance.common.data.download

import me.rhunk.snapenhance.common.config.impl.RootConfig
import java.text.SimpleDateFormat
import java.util.Locale


data class DashOptions(val offsetTime: Long, val duration: Long?)
data class AudioStreamFormat(val channels: Int, val sampleRate: Int, val encoding: Int)

data class InputMedia(
    val content: String,
    val type: DownloadMediaType,
    val encryption: MediaEncryptionKeyPair? = null,
    val attachmentType: String? = null,
    val isOverlay: Boolean = false,
)

data class DownloadRequest(
    val inputMedias: Array<InputMedia>,
    val dashOptions: DashOptions? = null,
    val audioStreamFormat: AudioStreamFormat? = null,
    private val flags: Int = 0,
) {
    object Flags {
        const val MERGE_OVERLAY = 1
        const val DASH_PLAYLIST = 2
        const val AUDIO_STREAM = 4
    }

    val isDashPlaylist: Boolean
        get() = flags and Flags.DASH_PLAYLIST != 0

    val shouldMergeOverlay: Boolean
        get() = flags and Flags.MERGE_OVERLAY != 0

    val isAudioStream: Boolean
        get() = flags and Flags.AUDIO_STREAM != 0
}

fun String.sanitizeForPath(): String {
    return this.replace(" ", "_")
        .replace(Regex("\\p{Cntrl}"), "")
}

fun createNewFilePath(
    config: RootConfig,
    hexHash: String,
    downloadSource: MediaDownloadSource,
    mediaAuthor: String?,
    creationTimestamp: Long?
): String {
    val pathFormat by config.downloader.pathFormat
    val customPathFormat by config.downloader.customPathFormat
    val sanitizedMediaAuthor = mediaAuthor?.sanitizeForPath() ?: hexHash
    val currentDateTime = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(creationTimestamp ?: System.currentTimeMillis())

    val finalPath = StringBuilder()

    fun appendFileName(string: String) {
        if (finalPath.isEmpty() || finalPath.endsWith("/")) {
            finalPath.append(string)
        } else {
            finalPath.append("_").append(string)
        }
    }

    if (customPathFormat.isNotEmpty()) {
        finalPath.append(customPathFormat
            .replace("%username%", sanitizedMediaAuthor)
            .replace("%source%", downloadSource.pathName)
            .replace("%hash%", hexHash)
            .replace("%date_time%", currentDateTime)
        )
    } else {
        if (pathFormat.contains("create_author_folder")) {
            finalPath.append(sanitizedMediaAuthor).append("/")
        }
        if (pathFormat.contains("create_source_folder")) {
            finalPath.append(downloadSource.pathName).append("/")
        }
        if (pathFormat.contains("append_hash")) {
            appendFileName(hexHash)
        }
        if (pathFormat.contains("append_source")) {
            appendFileName(downloadSource.pathName)
        }
        if (pathFormat.contains("append_username")) {
            appendFileName(sanitizedMediaAuthor)
        }
        if (pathFormat.contains("append_date_time")) {
            appendFileName(currentDateTime)
        }
    }

    if (finalPath.isEmpty() || finalPath.isBlank()) finalPath.append(hexHash)

    return finalPath.toString()
}