package data.ytdlp.model

import java.io.File
import java.io.InputStream

data class YtDlpResponse(
    val outInputStream: InputStream,
    val errorInputStream: InputStream,
    val createdFile: File? = null
) {
    companion object {
        fun empty() = YtDlpResponse(InputStream.nullInputStream(), InputStream.nullInputStream())
    }
}