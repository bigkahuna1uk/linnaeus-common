package com.springer.kotlin.undertow

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import com.springer.kotlin.json.*
import com.springer.kotlin.mapSecond
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.PathTemplateHandler
import java.net.URI

val CONTACT_EMAIL = Key("contact.email", stringType)


fun String.isSecret() = endsWith("password", ignoreCase = true)

fun Configuration.toJson(): JsonNode {
    return list().map {
        var (location, properties) = it
        com.springer.kotlin.json.ObjectNode(
            "description" to location.description.toJson(),
            "src" to location.uri?.toASCIIString()?.toJson(),
            "config" to properties
                .filterValues { !it.isSecret() }
                .mapValues { e -> TextNode(e.value) }.toJsonObject()
        )
    }.toJsonArray()
}

fun configHandler(config: Configuration) = HttpHandler { exchange ->
    exchange.sendJson(config.toJson())
}

fun versionHandler() = HttpHandler { exchange ->
    exchange.sendJson(ObjectNode(
        "revision" to (System.getenv("GIT_REVISION") ?: "unknown").toJson(),
        "build" to (System.getenv("BUILD_VERSION") ?: "unknown").toJson()
    ))
}


fun statusHandler(config: Configuration) = HttpHandler { exchange ->
    exchange.sendJson(ObjectNode(
        "contact" to com.springer.kotlin.json.ObjectNode(
            "email" to config.getOrElse(CONTACT_EMAIL, "<unknown>").toJson()
        )
    ))
}

sealed class Health(open val description: String?) {
    object Ok : Health(null)

    class Warn(override val description: String) : Health(description)

    class Error(override val description: String) : Health(description) {
        constructor(t: Throwable) : this(descriptionOf(t))
    }
}

private fun descriptionOf(t: Throwable): String {
    return "${t.javaClass.name}: ${t.message}\n${t.stackTrace.map { "  ${it}" }.joinToString("\n")}"
}

private fun reportHealth(healthChecks: Iterable<Pair<URI, () -> Health>>): List<Pair<URI, Health>> {
    return healthChecks.mapSecond { check ->
        try {
            check()
        } catch (e: Exception) {
            Health.Error(e)
        }
    }
}

fun dependenciesHandler(vararg namedChecks: Pair<URI, () -> Health>): HttpHandler {
    return HttpHandler { exchange ->
        val healthReports = reportHealth(namedChecks.asIterable())

        exchange.setResponseCode(httpStatusFor(healthReports))
        exchange.sendJson(healthReports.toDependenciesReportJson())
    }
}

fun Iterable<Pair<URI, Health>>.toDependenciesReportJson() =
    ObjectNode("results" to this.map(::dependencyReportJson).toJsonArray())


private fun dependencyReportJson(pair: Pair<URI, Health>): ObjectNode {
    val (uri, health) = pair

    return ObjectNode(
        "uri" to uri.toJson(),
        "status" to httpStatusFor(health).toJson(),
        "tag" to ArrayNode("required".toJson()),
        "description" to health.description?.toJson(),
        "level" to health.nameAsJson())
}

private fun httpStatusFor(health: Health) = if (health !is Health.Error) 200 else 503

fun httpStatusFor(healths: Iterable<Pair<URI, Health>>) =
    healths.map { it.second }.map { httpStatusFor(it) }.maxBy { it } ?: 200

private fun Health.nameAsJson(): TextNode {
    return when (this) {
        Health.Ok -> "OK"
        is Health.Warn -> "WARN"
        is Health.Error -> "ERROR"
    }.toJson()
}

fun PathTemplateHandler.appAnatomyHandlers(config: Configuration, vararg healthChecks: Pair<URI, () -> Health>) {
    path("/internal/status", "GET" to statusHandler(config))
    path("/internal/dependencies", "GET" to dependenciesHandler(*healthChecks))
    path("/internal/version", "GET" to versionHandler())
    path("/internal/config", "GET" to configHandler(config))
}
