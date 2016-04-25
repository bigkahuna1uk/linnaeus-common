package com.springer.refereify.util

import org.junit.Test
import kotlin.test.assertEquals


class ByteArraySourceInputStreamTest {

    private val buf = ByteArray(10)

    @Test fun empty() {
        val stream = ByteArraySourceInputStream(sourceOf())
        assertEquals(-1, stream.read())
        assertEquals(-1, stream.read(buf))
    }

    @Test fun one_byte() {
        val stream = ByteArraySourceInputStream(sourceOf(byteArrayOf(42)))
        assertEquals(42, stream.read())
        assertEquals(-1, stream.read())
    }

    @Test fun two_bytes_one_chunk() {
        val stream = ByteArraySourceInputStream(sourceOf(byteArrayOf(42, 84)))
        assertEquals(42, stream.read())
        assertEquals(84, stream.read())
        assertEquals(-1, stream.read())
    }

    @Test fun two_bytes_two_chunks() {
        val stream = ByteArraySourceInputStream(sourceOf(byteArrayOf(42), byteArrayOf(84)))
        assertEquals(42, stream.read())
        assertEquals(84, stream.read())
        assertEquals(-1, stream.read())
    }

    @Test fun two_bytes_two_chunks_bulk_read() {
        val stream = ByteArraySourceInputStream(sourceOf(byteArrayOf(42), byteArrayOf(84)))
        assertEquals(2, stream.read(buf))
        assertEquals(-1, stream.read())
        assertEquals(42, buf[0])
        assertEquals(84, buf[1])
        assertEquals(0,  buf[2])

        assertEquals(-1, stream.read(buf))
    }


    private fun sourceOf(vararg things: ByteArray): () -> ByteArray? {
        var i = 0
        return { if (i == things.size) null else things[i++] }
    }
}