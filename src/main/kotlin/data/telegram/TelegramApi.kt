package org.gusev.data.telegram

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import org.gusev.domain.model.VideoParams
import org.gusev.logger.Logger
import java.io.File
import kotlin.time.Duration.Companion.minutes

internal class TelegramApi(
    private val client: HttpClient = createClient(),
    private val logger: Logger,
    private val apiUrl: String,
    private val botToken: String
) {
    suspend fun sendVideo(
        chatId: String,
        videoFile: File,
        videoParams: VideoParams
    ) {
        val widthToHeight = videoParams.resolutionParsed()
        val videoResolution = "&width=${widthToHeight?.first}&height=${widthToHeight?.second}".takeIf { videoParams.resolution != null }.orEmpty()
        val apiUrl = "${apiUrl}bot$botToken/sendVideo?chat_id=$chatId&supports_streaming=true${videoResolution}"

        try {
            val response = client.submitFormWithBinaryData(
                url = apiUrl,
                formData = formData {
                    append(
                        key = "video",
                        value = InputProvider { videoFile.inputStream().asInput() },
                        headers = headers {
                            append(HttpHeaders.ContentType, ContentType.Video.MP4)
                            append(HttpHeaders.ContentDisposition, "filename=${videoFile.name}")
                        }
                    )
                }
            )

            logger.d(TAG) { "Video uploaded successfully: $response" }
        } catch (e: Exception) {
            logger.e(TAG) { "Error uploading video: ${e.message}" }
            throw e
        }
    }

    companion object {
        private const val TAG = "TelegramApi"
        fun createClient() = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30.minutes.inWholeMilliseconds
            }
        }
    }
}