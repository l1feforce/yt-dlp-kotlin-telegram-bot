package data.ytdlp.model

import kotlinx.coroutines.flow.Flow
import java.io.File

internal data class DownloadVideoResponse(
    val downloadedVideo: File,
    val progressBytes: Flow<String>
)
