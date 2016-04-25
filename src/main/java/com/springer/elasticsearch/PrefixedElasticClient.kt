package com.springer.elasticsearch

import org.elasticsearch.index.query.QueryBuilder
import java.time.Duration

class PrefixedElasticClient(private val indexPrefix: String, private val client: ElasticClient) : ElasticClient {
    override val defaultScrollLeaseTime: Duration = client.defaultScrollLeaseTime

    override fun <T : Any> get(indexType: IndexType<T>, id: Id<T>) =
        client.get(indexType.withPrefix(indexPrefix), id)

    override fun <T : Any> query(indexType: IndexType<T>, queryBuilder: QueryBuilder)
        = client.query(indexType.withPrefix(indexPrefix), queryBuilder)

    override fun <T : Any> scan(indexType: IndexType<T>, queryBuilder: QueryBuilder, pageSize: Int, scrollLeaseTime: Duration): Pair<Int, String> {
        return client.scan(indexType.withPrefix(indexPrefix), queryBuilder, pageSize, scrollLeaseTime)
    }

    override fun <T : Any> scroll(indexType: IndexType<T>, scrollId: String, scrollLeaseTime: Duration): Pair<String, List<Hit<T>>> {
        return client.scroll(indexType.withPrefix(indexPrefix), scrollId, scrollLeaseTime)
    }

    override fun <T : Any> scanAndScroll(indexType: IndexType<T>, queryBuilder: QueryBuilder, pageSize: Int, scrollLeaseTime: Duration)
        = client.scanAndScroll(indexType.withPrefix(indexPrefix), queryBuilder, pageSize, scrollLeaseTime)

    override fun <T : Any> index(indexType: IndexType<T>, thing: T, id: Id<T>?): Id<T> =
        client.index(indexType.withPrefix(indexPrefix), thing, id)

    override fun <T : Any> index(indexType: IndexType<T>, things: Sequence<T>) =
        client.index(indexType.withPrefix(indexPrefix), things)

    override fun <T> delete(indexType: IndexType<T>, id: Id<T>) =
        client.delete(indexType.withPrefix(indexPrefix), id)

    override fun deleteEntireIndex(indexType: IndexType<*>) =
        client.deleteEntireIndex(indexType.withPrefix(indexPrefix))

    override fun refresh(indexType: IndexType<*>) =
        client.refresh(indexType.withPrefix(indexPrefix))

    override fun indexHealth(indexName: String) =
        client.indexHealth(indexPrefix + indexName)

    override fun indexHealthChecker(indexName: String) =
        client.indexHealthChecker(indexPrefix + indexName)
}

fun ElasticClient.withPrefix(indexPrefix: String): ElasticClient = PrefixedElasticClient(indexPrefix, this)
