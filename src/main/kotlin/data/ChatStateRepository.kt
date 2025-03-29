package org.gusev.data

import com.github.kotlintelegrambot.entities.ChatId
import org.gusev.domain.model.ChatState
import org.gusev.logger.Logger
import java.util.concurrent.ConcurrentHashMap

internal class ChatStateRepository(
    private val logger: Logger
) {
    private val chatIdToStateMap = ConcurrentHashMap<ChatId.Id, ChatState>()

    fun setChatState(chatId: ChatId.Id, state: ChatState?) {
        chatIdToStateMap.compute(chatId) { _, lastState ->
            when (lastState) {
                is ChatState.VideoDownloadStarted -> lastState.videoDownloadJob.cancel()
                else -> {}
            }
            state.also {
                logger.d(TAG) { "Chat state changed for chatId=$chatId to $state" }
            }
        }
    }

    inline fun <reified T: ChatState> getCurrentState(chatId: ChatId.Id): T? =
        chatIdToStateMap[chatId] as? T

    companion object {
        private const val TAG = "ChatStateRepository"
    }
}