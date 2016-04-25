package com.springer.elasticsearch

data class Id<T>(val raw: String) {
    override fun toString() = raw
}

data class Hit<T>(val id: Id<T>, val value: T, val score: Float)

fun <T> Sequence<Hit<T>>.values() = map { it.value }

fun <T> Iterable<Hit<T>>.values() = map { it.value }
