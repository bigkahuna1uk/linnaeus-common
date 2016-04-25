package com.springer.kotlin.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import java.io.IOException
import java.net.URI

fun ObjectNode(block: ObjectNode.() -> Unit): ObjectNode = JsonNodeFactory.instance.objectNode().apply(block)
fun ArrayNode(block: ArrayNode.() -> Unit): ArrayNode = JsonNodeFactory.instance.arrayNode().apply(block)

fun ObjectNode(vararg entries: Pair<String, JsonNode?>) = ObjectNode(entries.asIterable())

fun ObjectNode(entries: Iterable<Pair<String, JsonNode?>>) = ObjectNode {
    for ((name, value) in entries) set(name, value)
}

fun ArrayNode(vararg elements: JsonNode?) = elements.toJsonArray()

fun <T> JsonNode.mapArrayNamed(fieldName: String, f: (JsonNode) -> T) = get(fieldName).map { f(it) }

infix fun String.colon(value: String) = Pair(this, value.toJson())
infix fun String.colon(value: Int) = Pair(this, value.toJson())
infix fun String.colon(value: Double) = Pair(this, value.toJson())
infix fun String.colon(elements: Array<out JsonNode?>) = Pair(this, elements.toJsonArray())

fun Boolean.toJson() = BooleanNode.valueOf(this)
fun Int.toJson() = IntNode(this)
fun Double.toJson() = DoubleNode(this)
fun String.toJson() = TextNode(this)
fun URI.toJson() = TextNode(this.toASCIIString())

fun Iterable<JsonNode?>.toJsonArray() = ArrayNode { for (e in this@toJsonArray) add(e) }
fun Array<out JsonNode?>.toJsonArray() = asIterable().toJsonArray()

fun Iterable<Pair<String, JsonNode?>>.toJsonObject() = ObjectNode(this)
fun Map<String, JsonNode?>.toJsonObject() = toList().toJsonObject()

fun JsonNode.getExpected(name: String): JsonNode = get(name) ?: throw MissingPropertyException("property '$name' is missing")
fun JsonNode.getExpected(index: Int): JsonNode = get(index) ?: throw MissingPropertyException("index $index out of range")

fun JsonNode.getNonBlankText(name: String) = getExpected(name).asText().apply {
    if (isBlank()) throw CouldNotParseException("property $name is blank")
}

fun JsonNode.getNonBlankText(index: Int) = getExpected(index).asText().apply {
    if (isBlank()) throw CouldNotParseException("element $index is blank")
}

fun JsonNode.getNonBlankText(arrayFieldName: String, index: Int): String {
    val array = getExpected(arrayFieldName)
    if (!array.has(index)) {
        throw CouldNotParseException("element $arrayFieldName[$index] does not exist")
    }

    val text = array.get(index).asText()
    if (text.isBlank()) throw CouldNotParseException("element $arrayFieldName[$index] is blank")

    return text
}

class MissingPropertyException(message: String) : CouldNotParseException(message)

open class CouldNotParseException : IOException {
    constructor(message: String) : super(message)
    constructor(message: String, root: Exception) : super(message, root)
}
