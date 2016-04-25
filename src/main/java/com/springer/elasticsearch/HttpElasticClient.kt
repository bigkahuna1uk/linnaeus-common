package com.springer.elasticsearch

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.springer.http.HttpPost
import com.springer.http.StreamingOutputEntity
import com.springer.http.checkSuccess
import com.springer.kotlin.flatSequence
import com.springer.kotlin.json.ObjectNode
import com.springer.kotlin.json.getNonBlankText
import com.springer.kotlin.undertow.Health
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.net.URI
import java.time.Duration

class HttpElasticClient(rootUri: URI,
                        private val httpClient: HttpClient = HttpClientBuilder.create().build(),
                        private val mapper: ObjectMapper = jacksonObjectMapper(),
                        override val defaultScrollLeaseTime: Duration = Duration.ofMinutes(5)
) : ElasticClient {

    private val uris = ElasticsearchUriScheme(rootUri)

    override fun <T : Any> get(indexType: IndexType<T>, id: Id<T>): T? {
        return httpClient.execute(HttpGet(uris.indexTypeUri(indexType, id))) { response ->
            if (response.statusLine.statusCode == 404) {
                null
            } else {
                response.checkSuccess()

                val json = response.readJsonBody()
                if (json["found"].asBoolean()) {
                    indexType.createFromJson(mapper, json["_source"])
                } else {
                    null
                }
            }
        }
    }

    override fun <T : Any> query(indexType: IndexType<T>, queryBuilder: QueryBuilder): List<Hit<T>> {
        return httpClient.execute(HttpPost(uris.searchUri(indexType), queryBuilder.asEntity())) { response ->
            parseHits(indexType, response)
        }
    }

    override fun <T : Any> index(indexType: IndexType<T>, thing: T, id: Id<T>?): Id<T> {
        val indexRequest = HttpPost(uris.indexTypeUri(indexType, id), StringEntity(thing.toJsonString(), APPLICATION_JSON))
        return httpClient.execute(indexRequest) { response ->
            response.checkSuccess()
            val json = response.readJsonBody()
            Id(json["_id"]?.asText() ?: throw JsonMappingException("no _id returned in response body"))
        }
    }

   override fun <T : Any> scan(
           indexType: IndexType<T>,
           queryBuilder: QueryBuilder,
           pageSize: Int,
           scrollLeaseTime: Duration
   ):
        Pair<Int,String>{
       val scanRequest = HttpPost(uris.scanAndScrollUri(indexType, scrollLeaseTime), queryBuilder.toRequest().size(pageSize).asEntity())
        return httpClient.execute(scanRequest) { response ->
            response.checkSuccess()
            val responseJson = response.readJsonBody()
            Pair(responseJson.totalHits(), responseJson.scrollId())
        }
    }

    override fun <T : Any> scroll(
            indexType: IndexType<T>,
            scrollId: String,
            scrollLeaseTime: Duration
    ):
           Pair<String, List<Hit<T>>> {

        return httpClient.execute(HttpGet(uris.fetchScrolledResultsUri(scrollId, scrollLeaseTime))) { response ->
            response.checkSuccess()
            val responseJson = response.readJsonBody()
            Pair(responseJson.scrollId(), responseJson.toSearchResults(indexType))
        }
    }

    override fun <T : Any> scanAndScroll(
            indexType: IndexType<T>,
            queryBuilder: QueryBuilder,
            pageSize: Int,
            scrollLeaseTime: Duration
    ):
        Sequence<Hit<T>> {
        val scanAndScrollRequest = HttpPost(uris.scanAndScrollUri(indexType, scrollLeaseTime), queryBuilder.toRequest().size(pageSize).asEntity())
        var (totalHits, initialScrollId) = httpClient.execute(scanAndScrollRequest) { response ->
            response.checkSuccess()
            val responseJson = response.readJsonBody()
            print(responseJson.totalHits())
            Pair(responseJson.totalHits(), responseJson.scrollId())
        }

        return if (totalHits == 0) emptySequence() else flatSequence(initialScrollId) { scrollId ->
            httpClient.execute(HttpGet(uris.fetchScrolledResultsUri(scrollId, scrollLeaseTime))) { response ->
                response.checkSuccess()
                val responseJson = response.readJsonBody()
                val results = responseJson.toSearchResults(indexType)

                if (results.size > 0) Pair(results, responseJson.scrollId()) else null
            }
        }
    }

    override fun <T : Any> index(indexType: IndexType<T>, things: Sequence<T>) {
        httpClient.execute(
            HttpPost(uris.bulkUri(), bulkRequestEntity(indexType.indexName, indexType.typeName, things)),
            HttpResponse::checkSuccess)
    }

    override fun <T> delete(indexType: IndexType<T>, id: Id<T>) {
        httpClient.execute(HttpDelete(uris.indexTypeUri(indexType, id)), HttpResponse::checkSuccess)
    }

    override fun deleteEntireIndex(indexType: IndexType<*>) {
        httpClient.execute(HttpDelete(uris.indexUri(indexType)), HttpResponse::checkSuccess)
    }

    override fun refresh(indexType: IndexType<*>) {
        httpClient.execute(HttpPost(uris.refreshUri(indexType)), HttpResponse::checkSuccess)
    }

    override fun indexHealth(indexName: String): Health {
        return httpClient.execute(HttpGet(uris.healthUri(indexName))) { response ->
            if (response.statusLine.statusCode != 200) {
                Health.Error("HTTP response ${response.statusLine.statusCode} ${response.statusLine.reasonPhrase}\n\n" +
                    EntityUtils.toString(response.entity))
            } else {
                val statusString = response.readJsonBody()["status"]?.asText()
                when (statusString) {
                    "green" -> Health.Ok
                    "yellow" -> Health.Warn("index in '$statusString' state")
                    "red" -> Health.Error("index in '$statusString' state")
                    else -> Health.Warn("unknown status: $statusString")
                }
            }
        }
    }

    override fun indexHealthChecker(indexName: String): Pair<URI, () -> Health> =
        uris.healthUri(indexName) to { indexHealth(indexName) }

    private fun <T : Any> parseHits(indexType: IndexType<T>, response: HttpResponse): List<Hit<T>> {
        response.checkSuccess()
        return response.readJsonBody().toSearchResults(indexType)
    }

    private fun <T : Any> JsonNode.toSearchResults(indexType: IndexType<T>): List<Hit<T>> {
        return this["hits"]["hits"].map { hit ->
            Hit(Id(hit.get("_id").asText()),
                indexType.createFromJson(mapper, hit.get("_source")),
                hit.get("_score").asDouble().toFloat())
        }
    }

    private fun <T> bulkRequestEntity(index: String, type: String, things: Sequence<T>) =
        StreamingOutputEntity(
            APPLICATION_JSON,
            things.asSequence().map { bulkIndexCommandBytes(index, type, it) })

    private fun <T> bulkIndexCommandBytes(indexName: String, typeName: String, thing: T) =
        ("${indexDirective(indexName, typeName)}\n" + "${thing.toJsonString()}\n").toByteArray()

    private fun indexDirective(indexName: String, typeName: String) = ObjectNode(
        "index" to ObjectNode(
            "_index" to TextNode(indexName),
            "_type" to TextNode(typeName)
        )
    ).toString()

    private fun Any?.toJsonString() = mapper.writeValueAsString(this)
    private fun HttpResponse.readJsonBody() = entity.content.use { input -> mapper.readTree(input) }
}

private fun SearchSourceBuilder.asEntity() = StringEntity(this.toString(), APPLICATION_JSON)

private fun QueryBuilder.asEntity() = this.toRequest().asEntity()

private fun QueryBuilder.toRequest(): SearchSourceBuilder = SearchSourceBuilder().query(this)

private fun JsonNode.scrollId() = getNonBlankText("_scroll_id")

private fun JsonNode.totalHits() = this["hits"]["total"].asInt()

