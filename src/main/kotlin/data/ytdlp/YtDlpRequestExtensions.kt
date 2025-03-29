package data.ytdlp

import data.ytdlp.model.YtDlpRequest
import org.gusev.domain.model.VideoParams

fun YtDlpRequest.OptionsBuilder.writeToFile(filePath: String) {
    addOption("-o", filePath)
}

fun YtDlpRequest.OptionsBuilder.getVideoInfo() {
    addOption("--dump-json", "")
}

internal fun YtDlpRequest.OptionsBuilder.getVideoWithParams(videoParams: VideoParams) {
    addOption("-f", videoParams.toYtDlpOptions())
}

fun YtDlpRequest.OptionsBuilder.update() {
    addOption("-U", "")
}

fun YtDlpRequest.OptionsBuilder.mergeOutputToOneFile() {
    addOption("--merge-output-format", "mkv")
}

fun YtDlpRequest.OptionsBuilder.stdErrAsProgress() {
    addOption("--newline")
}

fun YtDlpRequest.OptionsBuilder.ignoreExistedFiles() {
    addOption("--force-overwrites")
}
