package org.gusev.domain.model

import kotlin.random.Random

typealias Width = String
typealias Height = String

internal data class VideoParams(
    val videoId: String,
    val audioId: String? = null,
    val description: String = "",
    val fullSizeBytes: Long = FULL_SIZE_UNKNOWN,
    val title: String = Random.nextInt().toString(),
    val resolution: String? = null
) {
    fun toYtDlpOptions() = listOfNotNull(videoId, audioId).joinToString(VIDEO_AUDIO_DELIMITER)

    fun resolutionParsed(): Pair<Width, Height>? {
        val parsedResolution = resolution?.split(RESOLUTION_DELIMITER)
        var width = parsedResolution?.getOrNull(0)
        var height = parsedResolution?.getOrNull(1)

        // resolution is "1280x720"
        if (width != null && height != null) {
            return width to height
        }

        // resolution is "720p"
        height = resolution?.removeSuffix("p")
        val heightInt = height?.toIntOrNull() ?: return null

        width = (heightInt * (DEFAULT_SCREEN_RATIO)).toInt().toString()

        return width to height
    }

    companion object {
        const val VIDEO_AUDIO_DELIMITER = "+"
        const val FULL_SIZE_UNKNOWN = -1L

        private const val RESOLUTION_DELIMITER = "x"
        private const val DEFAULT_SCREEN_RATIO = 16f / 9f
        fun fromCallbackData(data: String): VideoParams {
            val parsedData = data.split(VIDEO_AUDIO_DELIMITER)
            val videoId = parsedData[0]
            val audioId = parsedData.getOrNull(1)

            return VideoParams(
                videoId = videoId,
                audioId = audioId
            )
        }
    }
}