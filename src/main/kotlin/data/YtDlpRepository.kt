package org.gusev.data

import data.ytdlp.*
import data.ytdlp.getVideoWithParams
import kotlinx.serialization.json.Json
import data.ytdlp.model.DownloadVideoResponse
import data.ytdlp.model.VideoInfo
import data.ytdlp.model.YtDlpRequest
import data.ytdlp.util.readInputStreamAsFlow
import org.gusev.domain.model.VideoParams
import org.gusev.domain.model.VideoParams.Companion.FULL_SIZE_UNKNOWN
import org.gusev.logger.Logger

internal class YtDlpRepository(
    private val logger: Logger,
    private val ytDlpApi: YtDlpApi,
    private val json: Json
) {
    suspend fun update() {
        logger.d(TAG) { "Updating yt-dlp" }
        ytDlpApi.executeRequest(YtDlpRequest {
            update()
        })
    }

    suspend fun getVideoParams(url: String): List<VideoParams> {
        logger.d(TAG) { "Requesting videoParams for url = $url" }
        val request = YtDlpRequest(url) {
            getVideoInfo()
        }

        val response = ytDlpApi.executeRequest(request)
        val videoInfoJson = response.outInputStream.bufferedReader().use { it.readText() }

        if (videoInfoJson.isEmpty()) {
            val errorText = response.errorInputStream.bufferedReader().use { it.readText() }
            logger.e(TAG) { "Failed to get videoParams: $errorText" }
            return emptyList()
        }

        val videoInfo = json.decodeFromString<VideoInfo>(videoInfoJson)
        logger.d(TAG) { "Parsed data = ${videoInfo.title}" }

        val bestAudio = videoInfo.findBestAudio()
        val videoAudioFormats = videoInfo.formats
            .filter { it.videoCodec != "none" && it.audioCodec != "none" }
            .map {
                VideoParams(
                    videoId = it.formatId,
                    description = it.formatLabel(),
                    fullSizeBytes = it.fileSize ?: FULL_SIZE_UNKNOWN,
                    title = videoInfo.title,
                    resolution = it.resolution
                )
            }
        val onlyVideoFormats = videoInfo.formats
            .filter { it.videoCodec != "none" && it.audioCodec == "none" }
            .mapNotNull {
                val fullSize = (it.fileSize ?: 0) + (bestAudio?.fileSize ?: 0)
                VideoParams(
                    videoId = it.formatId,
                    audioId = bestAudio?.formatId,
                    description = it.formatLabel(),
                    fullSizeBytes = if (fullSize > 0) fullSize else FULL_SIZE_UNKNOWN,
                    title = videoInfo.title,
                    resolution = it.resolution
                ).takeIf { bestAudio != null }
            }
        val fullList = (videoAudioFormats + onlyVideoFormats).toMutableList()

        // in case of empty videoCodec/audioCoded fields
        if (fullList.isEmpty()) {
            fullList += videoInfo.formats.map {
                VideoParams(
                    videoId = it.formatId,
                    description = it.formatLabel(),
                    fullSizeBytes = it.fileSize ?: FULL_SIZE_UNKNOWN,
                    title = videoInfo.title,
                    resolution = it.resolution
                )
            }
        }
        return fullList
    }

    suspend fun downloadVideo(
        url: String,
        videoParams: VideoParams,
        onResult: (Result<Unit>) -> Unit
    ): DownloadVideoResponse {
        logger.d(TAG) { "getVideoBytes for url = $url" }
        val request = YtDlpRequest(url, writeToFilePath = "${videoParams.title}.mkv") {
            getVideoWithParams(videoParams)
            mergeOutputToOneFile()
            stdErrAsProgress()
            ignoreExistedFiles()
        }

        val response = ytDlpApi.executeRequest(request, onResult)

        return DownloadVideoResponse(
            response.createdFile!!,
            response.errorInputStream.readInputStreamAsFlow()
        )
    }

    companion object {
        private const val TAG = "YtDlpRepository"
    }
}