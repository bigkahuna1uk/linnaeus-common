package com.springer.refereify.util

import com.springer.refereify.util.ChunkingIterator.Chunker.chunk
import org.junit.Assert.assertEquals
import org.junit.Test

class ChunkTest {

    @Test fun empty() {
        check(listOf())
    }

    @Test fun single() {
        check(listOf("1"),
                listOf("1"))
    }

    @Test fun single_with_separator() {
        check(listOf("*1"),
                listOf("*1"))
    }

    @Test fun two_in_one_chunk() {
        check(listOf("1", "2"),
                listOf("1", "2"))
    }

    @Test fun two_in_one_chunk_starting_with_separator() {
        check(listOf("*1", "2"),
                listOf("*1", "2"))
    }

    @Test fun two_in_two_chunks() {
        check(listOf("1", "*2"),
                listOf("1"),
                listOf("*2"))
    }

    @Test fun two_in_two_chunks_starting_with_separator() {
        check(listOf("*1", "*2"),
                listOf("*1"),
                listOf("*2"))
    }

    @Test fun four_in_two_chunks() {
        check(listOf("1", "2", "*3", "4"),
                listOf("1", "2"), listOf("*3", "4"))
    }

    private fun check(items: List<String>, vararg expected: List<String>) {
        assertEquals(listOf(*expected), chunk(items, { it.startsWith("*") }).toList())
    }
}

