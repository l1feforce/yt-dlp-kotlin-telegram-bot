package data.ytdlp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VideoFormat(
    @SerialName("format_id") val formatId: String,
    @SerialName("ext") val extension: String,
    @SerialName("resolution") val resolution: String? = null,
    @SerialName("video_ext") val videoCodec: String? = null,
    @SerialName("audio_ext") val audioCodec: String? = null,
    @SerialName("filesize_approx") val fileSize: Long? = null,
    @SerialName("fps") val framesPerSecond: Double? = null,
    @SerialName("tbr") val averageBitrate: Double? = null
) {
    fun formatLabel(): String {
        if (videoCodec == "none") return ""

        val parts = mutableListOf<String>()

        resolution?.takeUnless { it == "unknown" }?.let { parts += it }

        fileSize?.let { parts += "%.1fMb".format(it / 1024f / 1024f) }
            ?: averageBitrate?.let { parts += "%.1fMbps".format(it / 1000) }

        parts += extension.uppercase()

        return parts.joinToString(" ")
    }
}

@Serializable
internal data class VideoInfo(
    @SerialName("id") val videoId: String,
    @SerialName("title") val title: String,
    @SerialName("formats") val formats: List<VideoFormat>,
    @SerialName("duration") val durationSeconds: Int,
    @SerialName("thumbnail") val thumbnailUrl: String
) {
    fun findBestAudio(): VideoFormat? {
        val audioFormats = formats.filter {
            it.videoCodec == "none" &&
                    it.audioCodec != null &&
                    it.audioCodec != "none"
        }

        return audioFormats.maxWithOrNull(
            compareBy(
                { it.averageBitrate },

                { when (it.extension) {
                    "opus" -> 3
                    "m4a"  -> 2
                    "webm" -> 1
                    else   -> 0
                }},

                { -(it.fileSize ?: Long.MAX_VALUE) }
            )
        )
    }
}