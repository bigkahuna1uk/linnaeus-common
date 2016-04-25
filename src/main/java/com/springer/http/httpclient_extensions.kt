package com.springer.http

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.net.URI


fun HttpResponse.checkSuccess() = statusLine.run {
    if (statusCode >= 400) {
        throw IOException("${statusCode} ${reasonPhrase}: " +
            EntityUtils.toString(entity))
    }
}

fun HttpPost(uri: URI, entity: HttpEntity? = null) = HttpPost(uri).apply { this.entity = entity }
