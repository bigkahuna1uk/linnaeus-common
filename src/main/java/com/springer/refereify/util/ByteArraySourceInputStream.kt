package com.springer.refereify.util

import java.io.InputStream

class ByteArraySourceInputStream(val source: () -> ByteArray?) : InputStream() {
    var buf: ByteArray? = null
    var pos = -1
    override fun read(): Int {
        if (buf == null || pos >= buf!!.size) {
            buf = source.invoke()
            pos = 0
            if (buf == null)
                return -1
        }
        return buf!![pos++].toInt()
    }
}