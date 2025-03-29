package data.ytdlp.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

internal fun InputStream.readInputStreamAsFlow(): Flow<String> = channelFlow {
    val reader = BufferedReader(InputStreamReader(this@readInputStreamAsFlow))
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        line?.let { send(it) }
    }
    close()
}.flowOn(Dispatchers.IO)