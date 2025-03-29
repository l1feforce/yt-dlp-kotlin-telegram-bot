package ru.gusev.domain.model

import kotlinx.coroutines.Job

internal sealed interface ChatState {
    data class VideoParamsRequested(val url: String): ChatState
    data class VideoParamsCalculated(
        val url: String,
        val availableVideoParams: List<VideoParams>,
    ) : ChatState
    data class VideoDownloadStarted(
        val url: String,
        val videoParams: VideoParams,
        val videoDownloadJob: Job
    ): ChatState
}