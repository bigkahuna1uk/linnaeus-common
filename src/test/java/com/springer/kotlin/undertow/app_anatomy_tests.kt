package com.springer.kotlin.undertow

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThan
import com.oneeyedmen.okeydoke.junit.ApprovalsRule
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.net.URI

private fun uri(i: Int) = URI("http://example.com/" + i.toString())

class HealthErrorFormattingTest {
    @Test
    fun formats_exception_stacktrace() {
        val error = Health.Error(IOException("a failure occurred"))

        val lines = error.description.lines()

        assertThat("reports stacktrace after message", lines.size, greaterThan(1))
        assertThat("message line", lines[0], equalTo("java.io.IOException: a failure occurred"))
    }
}

class HealthToHttpStatusTest {
    @Test
    fun response_200_if_all_health_ok() {
        assertThat(httpStatusFor(listOf(uri(1) to Health.Ok, uri(2) to Health.Ok)), equalTo(200))
    }

    @Test
    fun response_200_if_health_warn_but_no_error() {
        assertThat(httpStatusFor(listOf(uri(1) to Health.Ok, uri(2) to Health.Warn("feeling poorly"))), equalTo(200))
    }

    @Test
    fun response_503_if_any_health_error() {
        assertThat(httpStatusFor(listOf(uri(1) to Health.Ok, uri(2) to Health.Error("dead"))), equalTo(503))
    }

}

class HealthToJsonTest {
    @Rule @JvmField public val approver = ApprovalsRule.fileSystemRule("src/test/java")

    @Test
    fun json() {
        val healthReports = listOf(
            uri(1) to Health.Ok,
            uri(2) to Health.Warn("warning!"),
            uri(3) to Health.Error("error!!")
        )

        val json = healthReports.toDependenciesReportJson()

        approver.transcript().append(jacksonObjectMapper().writer(DefaultPrettyPrinter().apply {
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        }).writeValueAsString(json))
    }
}