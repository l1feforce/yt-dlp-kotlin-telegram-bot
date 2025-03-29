package org.gusev

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import org.gusev.data.ChatStateRepository
import org.gusev.data.YtDlpRepository
import org.gusev.data.telegram.TelegramApi
import org.gusev.di.SimpleDiContainer
import org.gusev.domain.model.ChatState
import org.gusev.domain.model.VideoParams
import org.gusev.util.replyText
import org.gusev.util.sendMessage
import kotlin.time.Duration.Companion.seconds

private val logger = SimpleDiContainer.instance.logger

internal fun MessageHandlerEnvironment.handleGetVideoParams(
    url: String,
    ytDlpRepository: YtDlpRepository,
    chatStateRepository: ChatStateRepository,
) = appCoroutineScope.launch {
    val tag = "handleGetVideoParams"
    val chatId = ChatId.fromId(message.chat.id)
    if (chatStateRepository.getCurrentState<ChatState>(chatId) != null) {
        replyText("Already in work")
        return@launch
    }

    chatStateRepository.setChatState(chatId, ChatState.VideoParamsRequested(url))

    val videoParams = ytDlpRepository.getVideoParams(url)

    if (videoParams.isNotEmpty()) {
        logger.d(tag) { "videoParams = $videoParams" }
        val keyboardMarkup = InlineKeyboardMarkup.create(
            videoParams
                .map { DownloadVideoButton.create(it) }
                .chunked(2) + listOf(listOf(CancelVideoChooseButton.create()))
        )
        chatStateRepository.setChatState(chatId, ChatState.VideoParamsCalculated(url, videoParams))

        sendMessage("Choose video quality", replyMarkup = keyboardMarkup)
    } else {
        logger.e(tag) { "videoParams is empty" }

        chatStateRepository.setChatState(chatId, null)
        sendMessage("Failed to get video params")
    }
}

@OptIn(FlowPreview::class)
internal fun CallbackQueryHandlerEnvironment.handleDownloadVideo(
    chosenVideoParams: VideoParams,
    ytDlpRepository: YtDlpRepository,
    chatStateRepository: ChatStateRepository,
    telegramApi: TelegramApi
) = appCoroutineScope.launch {
    val tag = "handleDownloadVideo"
    val chatId = callbackQuery.message?.chat?.id?.let { ChatId.fromId(it) } ?: return@launch
    val messageId = callbackQuery.message?.messageId ?: return@launch

    val prevChatState = chatStateRepository.getCurrentState<ChatState.VideoParamsCalculated>(chatId)?: return@launch

    val selectedVideoParams =
        prevChatState.availableVideoParams.find { it.videoId == chosenVideoParams.videoId && it.audioId == chosenVideoParams.audioId } ?: chosenVideoParams

    val titleText = "Chosen format = ${selectedVideoParams.description}"
    val replyMarkup = InlineKeyboardMarkup.create(listOf(CancelVideoDownloadButton.create()))
    bot.editMessageText(chatId, messageId, text = "$titleText\nLoading started", replyMarkup = replyMarkup)

    val downloadVideoResponse = ytDlpRepository.downloadVideo(
        prevChatState.url,
        selectedVideoParams,
        onResult = {
            if (it.isFailure) {
                bot.editMessageText(chatId, messageId, text = "Error: ${it.exceptionOrNull()?.message}")
            }
        }
    )

    downloadVideoResponse.progressBytes.sample(3.seconds).collect { progressStr ->
        logger.d(tag) { progressStr }
        bot.editMessageText(chatId, messageId, text = "$titleText\n$progressStr", replyMarkup = replyMarkup)
    }

    bot.editMessageText(
        chatId,
        messageId,
        text = "$titleText\nUploading video to telegram...",
        replyMarkup = replyMarkup
    )
    telegramApi.sendVideo(
        chatId.id.toString(),
        videoFile = downloadVideoResponse.downloadedVideo,
        selectedVideoParams
    )
    bot.deleteMessage(chatId, messageId)
    downloadVideoResponse.downloadedVideo.delete()
    chatStateRepository.setChatState(chatId, null)
}
