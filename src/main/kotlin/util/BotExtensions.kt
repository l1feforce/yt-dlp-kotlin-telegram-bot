package ru.gusev.util

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup

internal fun MessageHandlerEnvironment.replyText(text: String) {
    bot.sendMessage(ChatId.fromId(message.chat.id), text = text)
}

internal fun MessageHandlerEnvironment.sendMessage(
    text: String,
    parseMode: ParseMode? = null,
    disableWebPagePreview: Boolean? = null,
    disableNotification: Boolean? = null,
    protectContent: Boolean? = null,
    replyToMessageId: Long? = null,
    allowSendingWithoutReply: Boolean? = null,
    replyMarkup: ReplyMarkup? = null,
    messageThreadId: Long? = null,
) {
    bot.sendMessage(
        ChatId.fromId(message.chat.id),
        text,
        parseMode,
        disableWebPagePreview,
        disableNotification,
        protectContent,
        replyToMessageId,
        allowSendingWithoutReply,
        replyMarkup,
        messageThreadId
    )
}