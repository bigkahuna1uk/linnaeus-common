package com.springer.elasticsearch

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

interface IndexType<T> {
    val indexName: String
    val typeName: String
    fun createFromJson(objectMapper: ObjectMapper, jsonNode: JsonNode): T
}

inline fun <reified T : Any> IndexType(indexName: String, typeName: String) =
    object : IndexType<T> {
        override val indexName: String = indexName
        override val typeName: String = typeName
        override fun createFromJson(objectMapper: ObjectMapper, jsonNode: JsonNode): T =
            objectMapper.treeToValue(jsonNode, T::class.java)
    }

fun <T, U> IndexType<T>.map(fn: (T) -> U): IndexType<U> =
    object : IndexType<U> {
        override val indexName: String = this@map.indexName
        override val typeName: String = this@map.typeName

        override fun createFromJson(objectMapper: ObjectMapper, jsonNode: JsonNode): U =
            fn(this@map.createFromJson(objectMapper, jsonNode))
    }

fun <T> IndexType<T>.withPrefix(prefix: String) = object : IndexType<T> by this {
    override val indexName: String = prefix + this@withPrefix.indexName
}
