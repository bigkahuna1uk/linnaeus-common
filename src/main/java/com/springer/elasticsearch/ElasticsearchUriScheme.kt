package com.springer.elasticsearch

import java.net.URI
import java.time.Duration
import java.util.*

class ElasticsearchUriScheme(val rootUri: URI) {
    fun indexUri(indexType: IndexType<*>) = resolve(indexType.indexName)

    fun <T> indexTypeUri(indexType: IndexType<T>, id: Id<T>? = null) = resolve(indexType.toPath(id?.raw))

    fun searchUri(indexType: IndexType<*>) = resolve(indexType.toPath("_search"))

    fun scanAndScrollUri(indexType: IndexType<*>, scrollLeasetime: Duration) =
        searchUri(indexType).resolve("./_search?search_type=scan&scroll=${scrollLeasetime.asUrlParam()}")

    fun fetchScrolledResultsUri(scrollId: String, scrollLeasetime: Duration) =
        resolve("/_search/scroll?scroll=${scrollLeasetime.asUrlParam()}&scroll_id=${scrollId}")

    fun bulkUri() = resolve("_bulk")

    fun refreshUri(indexType: IndexType<*>) = resolve(indexType.indexName + "/_refresh")

    fun healthUri(indexName: String) = resolve("/_cluster/health/$indexName")

    private fun resolve(s: String): URI = rootUri.resolve(s)

    private fun IndexType<*>.toPath(rest: String? = null) = "/${indexName}/${typeName}/${rest ?: ""}"

    private fun Duration.asUrlParam() = toString().substring(2).toLowerCase(Locale.ROOT)
}