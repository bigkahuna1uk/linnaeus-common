package com.springer.refereify.util

class ChunkingIterator<T>(private val things: Iterator<T>, private val isSeparator: (T) -> Boolean) : Iterator<List<T>> {

    var chunk = arrayListOf<T>()
    var firstItem = true

    override fun hasNext(): Boolean {
        return things.hasNext() || chunk.isNotEmpty()
    }

    override fun next(): List<T> {

        while (things.hasNext()) {
            val next = things.next()
            if (isSeparator(next) && !firstItem) {
                firstItem = false
                return if (chunk.isEmpty()) arrayListOf(next) else replaceChunk(next)
            } else {
                firstItem = false
                chunk.add(next)
            }
        }
        return replaceChunk()
    }

    private fun replaceChunk(vararg items: T): MutableList<T> {
        val result = chunk
        chunk = arrayListOf(*items)
        return result
    }

    companion object Chunker {
        fun <T> chunk(things: Sequence<T>, isSeparator: (T) -> Boolean): Sequence<List<T>> {
            return ChunkingIterator(things.iterator(), isSeparator).asSequence()
        }

        fun <T> chunk(things: Iterable<T>, isSeparator: (T) -> Boolean): Iterable<List<T>> {
            return ChunkingIterator(things.iterator(), isSeparator).asSequence().asIterable()
        }
    }
}