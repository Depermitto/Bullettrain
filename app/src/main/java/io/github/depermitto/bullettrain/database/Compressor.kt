package io.github.depermitto.bullettrain.database

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object Compressor {
    /**
     * Compress data using gzip and encode with base64. This is equivalent to using
     * `echo string | gzip | base64 > file`
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun compress(string: String): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            gzipOutputStream.write(string.toByteArray())
        }
        return Base64.encode(byteArrayOutputStream.toByteArray())
    }

    /**
     * Decompress base64-encoded gzipped data. This is equivalent to using
     * `base64 -d file | gunzip`
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun uncompress(string: String): String {
        val byteArrayInputStream = ByteArrayInputStream(Base64.decode(string))
        GZIPInputStream(byteArrayInputStream).use { gzipInputStream ->
            return gzipInputStream.readBytes().toString(Charset.defaultCharset())
        }
    }
}