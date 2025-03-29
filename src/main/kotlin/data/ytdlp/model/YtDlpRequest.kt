package data.ytdlp.model

import data.ytdlp.writeToFile

data class YtDlpRequest(
    val url: String = "",
    val writeToFilePath: String? = null,
    val optionsBuilder: OptionsBuilder.() -> Unit
) {
    private val commandOptions = OptionsBuilder().apply(optionsBuilder)

    init {
        if (writeToFilePath != null) {
            commandOptions.writeToFile(writeToFilePath)
        }
    }

    fun buildCommand(): List<String> =
        buildList {
            add(YT_DLP_START_COMMAND)
            addAll(commandOptions.commandOptions.entries.flatMap { listOf(it.key, it.value) }.filter { it.isNotEmpty() })
            add(url)
        }

    class OptionsBuilder(
        val commandOptions: MutableMap<String, String> = mutableMapOf()
    ) {
        fun addOption(key: String) {
            commandOptions[key] = ""
        }

        fun addOption(key: String, value: String) {
            commandOptions[key] = value
        }
    }

    companion object {
        private const val YT_DLP_START_COMMAND = "yt-dlp"
    }
}