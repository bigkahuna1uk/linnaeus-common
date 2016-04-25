package com.springer.kotlin.undertow

import com.fasterxml.jackson.databind.JsonNode
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.PathTemplateHandler
import io.undertow.server.handlers.ResponseCodeHandler
import io.undertow.util.HeaderMap
import io.undertow.util.Headers.CONTENT_TYPE
import io.undertow.util.HttpString

fun server(block: Undertow.Builder.() -> Unit) = Undertow.builder().apply(block).build()

fun Undertow.Builder.http(port: Int, hostname: String) = addHttpListener(port, hostname)

fun paths(block: PathTemplateHandler.() -> Unit) = PathTemplateHandler(true).apply(block)

fun Undertow.Builder.route(handler: HttpHandler) {
    setHandler(handler)
}

fun PathTemplateHandler.path(template: String, vararg methodHandlers: Pair<String, HttpHandler>) {
    add(template, methods(*methodHandlers))
}

infix fun String.to(handler: HttpServerExchange.()->Unit) = Pair(this, HttpHandler { exchange -> exchange.handler() })

fun methods(vararg methodHandlers: Pair<String, HttpHandler>) = HttpHandler { exchange ->
    for ((method, handler) in methodHandlers) {
        if (exchange.requestMethod.toString().equals(method, ignoreCase = true)) {
            handler.handleRequest(exchange)
            return@HttpHandler
        }
    }

    ResponseCodeHandler.HANDLE_405.handleRequest(exchange)
}

fun HttpServerExchange.sendJson(body: JsonNode) {
    responseHeaders.put(CONTENT_TYPE, "application/json")
    responseSender.send(body.toString())
}

fun HeaderMap.add(name: String, value: String) = add(HttpString(name), value)
