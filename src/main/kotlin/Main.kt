package ru.gusev

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.*
import ru.gusev.data.YtDlpRepository
import ru.gusev.di.SimpleDiContainer
import ru.gusev.domain.model.ChatState
import ru.gusev.domain.model.VideoParams
import ru.gusev.util.replyText
import kotlin.time.Duration.Companion.hours

internal val appCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun main() {
    val diContainer = SimpleDiContainer.instance

    val logger = diContainer.logger
    val validateUrlUseCase = diContainer.validateUrlUseCase
    val ytDlpRepository = diContainer.ytDlpRepository
    val chatStateRepository = diContainer.chatStateRepository
    val telegramApi = diContainer.telegramApi

    val bot = bot {
        token = SimpleDiContainer.BOT_TOKEN
        apiUrl = SimpleDiContainer.TELEGRAM_API_URL
        logLevel = LogLevel.Error
        dispatch {
            message(authFilter(SimpleDiContainer.AUTHORIZED_USERS)) {
                val url = message.text.orEmpty()

                if (!validateUrlUseCase.isValidUrl(url)) {
                    replyText("It is not a url, send the url with a video")
                    return@message
                }

               handleGetVideoParams(url, ytDlpRepository, chatStateRepository)
            }

            callbackQuery {
                if (callbackQuery.data in alreadyUsedCallbacks) {
                    return@callbackQuery
                }

                val chatId = callbackQuery.message?.chat?.id?.let { ChatId.fromId(it) } ?: return@callbackQuery
                val prevChatState = chatStateRepository.getCurrentState<ChatState.VideoParamsCalculated>(chatId) ?: return@callbackQuery
                val videoParams = VideoParams.fromCallbackData(callbackQuery.data)

                val downloadVideoJob = handleDownloadVideo(videoParams, ytDlpRepository, chatStateRepository, telegramApi)

                chatStateRepository.setChatState(
                    chatId, ChatState.VideoDownloadStarted(
                        url = prevChatState.url,
                        videoParams = videoParams,
                        videoDownloadJob = downloadVideoJob
                    )
                )
            }

            callbackQuery(CancelVideoChooseButton.callbackData) {
                logger.d(CancelVideoChooseButton.callbackData) { "Cancel choose query" }
                appCoroutineScope.launch {
                    val chatId = callbackQuery.message?.chat?.id?.let { ChatId.fromId(it) } ?: return@launch
                    val messageId = callbackQuery.message?.messageId ?: return@launch

                    bot.deleteMessage(chatId, messageId)
                    chatStateRepository.setChatState(chatId, null)

                }
            }

            callbackQuery(CancelVideoDownloadButton.callbackData) {
                logger.d(CancelVideoDownloadButton.callbackData) { "Cancel download query" }
                appCoroutineScope.launch {
                    val chatId = callbackQuery.message?.chat?.id?.let { ChatId.fromId(it) } ?: return@launch
                    val messageId = callbackQuery.message?.messageId ?: return@launch

                    bot.deleteMessage(chatId, messageId)
                    chatStateRepository.setChatState(chatId, null)
                }
            }
        }
    }
    startPeriodicalYtDlpUpdate(ytDlpRepository)
    bot.startPolling()
}

private fun startPeriodicalYtDlpUpdate(ytDlpRepository: YtDlpRepository) = appCoroutineScope.launch {
    while (isActive) {
        ytDlpRepository.update()
        delay(24.hours)
    }
}
