package io.github.depermitto.bullettrain.database

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object Compressor {
    @OptIn(ExperimentalEncodingApi::class)
    fun compress(string: String): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            gzipOutputStream.write(string.toByteArray())
        }
        return Base64.encode(byteArrayOutputStream.toByteArray())
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun uncompress(string: String): String {
        val byteArrayInputStream = ByteArrayInputStream(Base64.decode(string))
        GZIPInputStream(byteArrayInputStream).use { gzipInputStream ->
            return gzipInputStream.readBytes().toString(Charset.defaultCharset())
        }
    }
}