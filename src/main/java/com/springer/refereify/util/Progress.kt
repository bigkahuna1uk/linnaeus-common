package com.springer.refereify.util

import java.io.PrintStream


class Progress(
        val char: Char = '.',
        val printStream: PrintStream = System.out,
        val consoleWidth: Int = 80) {

    var position = 0

    fun tick() {
        tick(char)
    }

    fun tick(tickChar: Char) {
        printStream.print(tickChar)
        if (++position == consoleWidth) printStream.println()
        position %= consoleWidth
    }

    fun phase(name: String) {
        if (position > 0)
            println()
        println(name)
        position = 0
    }

    fun phase(name: String, action: ()->Unit) {
        phase(name)
        action()
        phase("finished " + name)
    }
}