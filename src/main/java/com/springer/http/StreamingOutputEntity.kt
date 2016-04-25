package com.springer.http

import org.apache.http.entity.AbstractHttpEntity
import org.apache.http.entity.ContentType
import java.io.InputStream
import java.io.OutputStream

class StreamingOutputEntity(contentType: ContentType, private val chunks: Sequence<ByteArray>) : AbstractHttpEntity() {
    init {
        setContentType(contentType.toString())
    }

    override fun isRepeatable() = true
    override fun getContentLength() = -1L
    override fun getContent(): InputStream {
        throw UnsupportedOperationException()
    }

    override fun isStreaming() = true

    override fun writeTo(os: OutputStream) {
        chunks.forEach { os.write(it) }
    }
}