package com.springer.kotlin.undertow

import io.undertow.server.HttpHandler

fun corsify(wrapped: HttpHandler): HttpHandler {
    return HttpHandler { exchange ->
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*");
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, location");
        exchange.responseHeaders.add("Access-Control-Allow-Credentials", "true");
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.responseHeaders.add("Access-Control-Max-Age", "1209600");
        wrapped.handleRequest(exchange)
    }
}
