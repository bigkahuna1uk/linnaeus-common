package com.springer.elasticsearch

import com.springer.kotlin.onExit
import com.springer.kotlin.timeBlock
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.Node
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import java.io.File
import java.net.URI

private val DEFAULT_DATA_DIRECTORY = "target/elasticsearch-data"
private val HTTP_PORT = "19200"

class EmbeddedElasticsearchServer(private val dataDirectory: String = DEFAULT_DATA_DIRECTORY) : AutoCloseable {
    private val node: Node

    init {
        deleteDataDirectory()
        val elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("http.enabled", "true")
                .put("http.port", HTTP_PORT)
                .put("path.data", dataDirectory)
                .put("path.home", dataDirectory)

        node = nodeBuilder().local(true).settings(elasticsearchSettings.build()).node()
    }

    override fun close() {
        node.close()
    }

    private fun deleteDataDirectory() {
        File(dataDirectory).deleteRecursively()
    }

    fun rootUri() = URI("http://localhost:$HTTP_PORT/")

    fun client(prefix: String = "") = HttpElasticClient(rootUri()).withPrefix(prefix)
}

val embeddedElasticSearchServer by lazy {
    timeBlock("Starting Elastic Search") {
        EmbeddedElasticsearchServer()
    }
} onExit { it.close() }
