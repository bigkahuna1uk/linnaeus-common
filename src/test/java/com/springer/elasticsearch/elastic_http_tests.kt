package com.springer.elasticsearch

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.springer.kotlin.onExit
import org.apache.http.impl.client.HttpClientBuilder
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.junit.BeforeClass
import org.junit.Test
import java.net.URI
import java.time.Duration
import kotlin.test.assertNotNull

data class Example(val i: Int)

val examples = IndexType<Example>("examples", "example")

val http by lazy { HttpClientBuilder.create().build() } onExit { it.close() }


class ElasticsearchIndexTest {
    val client = HttpElasticClient(embeddedElasticSearchServer.rootUri(), httpClient = http).withPrefix("${System.currentTimeMillis()}_")

    @Test
    fun can_index_one_thing() {
        val id = client.index(examples, Example(1001))

        client.refresh(examples)

        assertThat(client.get(examples, id), present(equalTo(Example(1001))))
    }

    @Test
    fun can_index_one_thing_with_explicit_id() {
        val id = client.index(examples, Example(1002), Id<Example>("xxx"))
        assertThat(id, equalTo(Id<Example>("xxx")))

        client.refresh(examples)

        assertThat(client.get(examples, id), present(equalTo(Example(1002))))
    }
}

class ElasticsearchDeleteTest {
    val client = HttpElasticClient(embeddedElasticSearchServer.rootUri(), httpClient = http).withPrefix("${System.currentTimeMillis()}_")

    @Test
    fun can_delete_indexed_document() {
        val idA = client.index(examples, Example(1003))
        val idB = client.index(examples, Example(1004))
        client.refresh(examples)

        client.delete(examples, idA)

        client.refresh(examples)

        assertThat(client.get(examples, idA), absent())
        assertThat(client.get(examples, idB), present(equalTo(Example(1004))))
    }
}

class ElasticsearchScanAndScrollTest {
    companion object {
        val exampleCount = 1000
        val client = HttpElasticClient(embeddedElasticSearchServer.rootUri(), httpClient = http).withPrefix("${System.currentTimeMillis()}_")

        @BeforeClass @JvmStatic
        fun setUpExampleData() {
            println("creating index")
            client.index(examples, (1..exampleCount).map { i -> Example(i) })
            client.refresh(examples)
        }
    }

    @Test
    fun query_returns_subset_of_data() {
        val results = client.query(examples, matchAllQuery())

        assertThat(results.size, lessThan(exampleCount))
    }

    @Test
    fun scan_and_scroll_data_page_by_page() {
        val (totalHits, scrollId) = client.scan(examples, matchAllQuery())
        assertThat(totalHits, equalTo(exampleCount))
        assertNotNull(scrollId)

        var results = listOf<Hit<Example>>()
        var currentScrollId = scrollId

        while(true) {
            val(scrollId, scrollResults) = client.scroll(examples, currentScrollId)
            if(scrollResults.size == 0) break
            currentScrollId = scrollId
            results += scrollResults
        }

        assertThat(results.size, equalTo(exampleCount))
        assertNotNull(scrollId)
    }

    @Test
    fun scan_and_scroll_returns_all_data() {
        val results = client.scanAndScroll(examples, matchAllQuery()).toList()

        assertThat(results.size, equalTo(exampleCount))
    }

}

class ElasticsearchUriGenerationTest {
    val uris = ElasticsearchUriScheme(URI("http://example.com:1234"))
    val indexType = IndexType<String>("things", "thing")

    @Test
    fun scan_and_scroll_uri_contains_lease_duration() {
        assertThat("specified in seconds",
            uris.scanAndScrollUri(indexType, Duration.ofSeconds(20)), hasParameter("scroll", "20s"))
        assertThat("minutes specified in seconds",
            uris.scanAndScrollUri(indexType, Duration.ofSeconds(120)), hasParameter("scroll", "2m"))
        assertThat("specified in minutes",
            uris.scanAndScrollUri(indexType, Duration.ofMinutes(5)), hasParameter("scroll", "5m"))
    }

    @Test
    fun fetch_scrolled_results_uri_contains_lease_duration() {
        assertThat("specified in seconds",
            uris.fetchScrolledResultsUri("id", Duration.ofSeconds(20)), hasParameter("scroll", "20s"))
        assertThat("minutes specified in seconds",
            uris.fetchScrolledResultsUri("id", Duration.ofSeconds(120)), hasParameter("scroll", "2m"))
        assertThat("specified in minutes",
            uris.fetchScrolledResultsUri("id", Duration.ofMinutes(5)), hasParameter("scroll", "5m"))
    }

    private fun hasParameter(name: String, value: String) = has(URI::toASCIIString, contains(Regex("[?&]$name=$value(&|$)")))
}