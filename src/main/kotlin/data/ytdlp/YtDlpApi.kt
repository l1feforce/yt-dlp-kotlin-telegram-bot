package data.ytdlp

import kotlinx.coroutines.*
import data.ytdlp.model.YtDlpRequest
import data.ytdlp.model.YtDlpResponse
import org.gusev.logger.Logger
import java.io.File

class YtDlpApi(
    private val logger: Logger,
    private val coroutineScope: CoroutineScope
) {
    suspend fun executeRequest(
        request: YtDlpRequest,
        result: (Result<Unit>) -> Unit = {}
    ): YtDlpResponse = withContext(Dispatchers.IO) {
        val processBuilder = ProcessBuilder(
            request
                .buildCommand()
                .also { logger.d(TAG) { "Built command: ${it.joinToString(" ")}" } }
        ).redirectOutput(ProcessBuilder.Redirect.PIPE)

        val processResult = kotlin.runCatching {
            processBuilder.start()
        }

        if (processResult.isFailure) {
            result(Result.failure(processResult.exceptionOrNull() ?: RuntimeException("Failed to start process")))
            return@withContext YtDlpResponse.empty()
        }

        val process = processResult.getOrNull()!!

        coroutineScope.launch {
            val processExitCode = process.waitFor()

            if (processExitCode != 0) {
                result(Result.failure(RuntimeException("yt-dlp error (code $processExitCode)")))
            }
        }

        var downloadedFile: File? = null
        val outStream =
            if (request.writeToFilePath != null) {
                createFile(request.writeToFilePath).let {
                    downloadedFile = it
                    it.inputStream()
                }
            } else {
                process.inputStream
            }

        val errStream =
            if (request.writeToFilePath != null) {
                process.inputStream
            } else {
                process.errorStream
            }

        return@withContext YtDlpResponse(
            outInputStream = outStream,
            errorInputStream = errStream,
            createdFile = downloadedFile
        )
    }

    private fun createFile(path: String): File {
        val file = File(path)

        if (!file.exists()) {
            file.createNewFile()
        }

        return File(path)
    }

    companion object {
        private const val TAG = "YtDlpApi"
    }
}