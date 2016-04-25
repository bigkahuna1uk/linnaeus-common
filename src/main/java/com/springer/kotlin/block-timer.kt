package com.springer.kotlin

fun <T> timeBlock(name: String, reporter: (String, Long) -> Unit = ::defaultReporter, f: () -> T): T {
    val start = System.currentTimeMillis()
    try {
        return f()
    } finally {
        reporter(name, System.currentTimeMillis() - start)
    }
}

private fun defaultReporter(name: String, durationMs: Long) = System.err.println("$name took $durationMs ms")

