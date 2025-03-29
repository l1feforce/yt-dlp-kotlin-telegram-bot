package ru.gusev

import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ru.gusev.domain.model.VideoParams

internal object CancelVideoChooseButton {
    const val text = "Cancel"
    const val callbackData = "cancel_video_choose"

    fun create() = InlineKeyboardButton.CallbackData(
        text = text,
        callbackData = callbackData
    )
}

internal object CancelVideoDownloadButton {
    const val text = "Cancel"
    const val callbackData = "cancel_video_download"

    fun create() = InlineKeyboardButton.CallbackData(
        text = text,
        callbackData = callbackData
    )
}

internal object DownloadVideoButton {
    const val callbackData = "downloadVideo"

    fun create(videoParams: VideoParams) = InlineKeyboardButton.CallbackData(
        text = videoParams.description,
        callbackData = videoParams.toYtDlpOptions()
    )
}

internal val alreadyUsedCallbacks = setOf(
    CancelVideoChooseButton.callbackData,
    DownloadVideoButton.callbackData,
    CancelVideoDownloadButton.callbackData
)