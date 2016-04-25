package com.springer.kotlin

fun<T> identity(): (T) -> T = { t: T -> t }

fun <T> Iterable<T>.zipWithIndex(): Iterable<Pair<T, Int>> = this.zip(0..Int.MAX_VALUE)
fun <T> Sequence<T>.zipWithIndex(): Sequence<Pair<T, Int>> = this.zip((0..Int.MAX_VALUE).asSequence())

infix fun <T> T?.orElse(other: T): T {
    return this ?: other
}

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        close()
    }
}

fun <T, U> withIt(block: T.() -> U): (T) -> U = { it.block() }
fun <T, U> toThis(block: (T) -> U): T.() -> U = { block(this) }


infix fun <T> Lazy<T>.onExit(cleanup: (T) -> Unit): Lazy<T> {
    Runtime.getRuntime().addShutdownHook(Thread {
        if (isInitialized()) cleanup(value)
    })
    return this
}

fun Any.formattedAs(formatString: String) = formatString.format(this)

fun <F, S, F2> Pair<F, S>.mapFirst(fn: (F) -> F2) = Pair(fn(first), second)
fun <F, S, F2> Iterable<Pair<F, S>>.mapFirst(fn: (F) -> F2) = map { it.mapFirst(fn) }

fun <F, S, S2> Pair<F, S>.mapSecond(fn: (S) -> S2) = Pair(first, fn(second))

fun <F,S> Pair<F,S?>.withSecondOrNull() : Pair<F,S>? = second?.let{first to it}

fun <F, S, S2> Iterable<Pair<F, S>>.mapSecond(fn: (S) -> S2) = map { it.mapSecond(fn) }

fun <T, R> Sequence<T>.collectTo(accumulator: R, accumulation: (R, T) -> Unit) =
    this.fold(accumulator, { accumulator, item -> accumulation(accumulator, item); accumulator })

fun <T, R> Iterable<T>.collectTo(accumulator: R, accumulation: (R, T) -> Unit) =
    this.fold(accumulator, { accumulator, item -> accumulation(accumulator, item); accumulator })


fun <R, S> sequence(initialSeed: S, generator: (S) -> Pair<R, S>?) =
    generateSequence(generator(initialSeed), { p: Pair<R, S> -> generator(p.second) }).map { it.first }

fun <R, S> flatSequence(initialSeed: S, generator: (S) -> Pair<Iterable<R>, S>?) =
    sequence(initialSeed, generator).flatten()
