package ru.gusev.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import ru.gusev.data.ChatStateRepository
import ru.gusev.data.YtDlpRepository
import ru.gusev.data.telegram.TelegramApi
import data.ytdlp.YtDlpApi
import ru.gusev.domain.ValidateUrlUseCase
import ru.gusev.logger.DefaultLogger
import ru.gusev.util.getEnvVariable

internal class SimpleDiContainer(
    private val telegramApiUrl: String,
    private val botToken: String
) {
    val logger by lazy {
        DefaultLogger()
    }
    val validateUrlUseCase by lazy {
        ValidateUrlUseCase()
    }
    val ioScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
    val ytDlpApi by lazy {
        YtDlpApi(logger, ioScope)
    }
    val json by lazy {
        Json { ignoreUnknownKeys = true }
    }
    val ytDlpRepository by lazy {
        YtDlpRepository(logger, ytDlpApi, json)
    }
    val chatStateRepository by lazy {
        ChatStateRepository(logger)
    }
    val telegramApi by lazy {
        TelegramApi(apiUrl = telegramApiUrl, botToken = botToken, logger = logger)
    }

    companion object {
        val BOT_TOKEN = getEnvVariable("BOT_TOKEN").orEmpty()
        val TELEGRAM_API_URL = getEnvVariable("TELEGRAM_API_URL") ?: "https://api.telegram.org/"
        val AUTHORIZED_USERS =
            (getEnvVariable("USERS") ?: "")
                .split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .toSet()

        val instance by lazy {
            SimpleDiContainer(telegramApiUrl = TELEGRAM_API_URL, botToken = BOT_TOKEN)
        }
    }
}