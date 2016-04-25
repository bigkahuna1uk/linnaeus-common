package com.springer.elasticsearch

import com.springer.kotlin.undertow.Health
import org.elasticsearch.index.query.QueryBuilder
import java.net.URI
import java.time.Duration

interface ElasticClient {
    val defaultScrollLeaseTime: Duration

    fun <T: Any> get(indexType: IndexType<T>, id: Id<T>): T?
    fun <T : Any> query(indexType: IndexType<T>, queryBuilder: QueryBuilder): List<Hit<T>>

    fun <T : Any> scan(indexType: IndexType<T>, queryBuilder: QueryBuilder,
                       pageSize: Int = 100, scrollLeaseTime: Duration = defaultScrollLeaseTime):
            Pair<Int,String>

    fun <T : Any> scroll(indexType: IndexType<T>, scrollId: String, scrollLeaseTime: Duration = defaultScrollLeaseTime):
          Pair<String, List<Hit<T>>>

    fun <T : Any> scanAndScroll(indexType: IndexType<T>, queryBuilder: QueryBuilder,
                                pageSize: Int = 100,
                                scrollLeaseTime: Duration = defaultScrollLeaseTime):
            Sequence<Hit<T>>

    fun <T : Any> index(indexType: IndexType<T>, thing: T, id: Id<T>? = null): Id<T>
    fun <T : Any> index(indexType: IndexType<T>, things: Iterable<T>) = index(indexType, things.asSequence())
    fun <T : Any> index(indexType: IndexType<T>, things: Sequence<T>)
    fun <T> delete(indexType: IndexType<T>, id: Id<T>)

    fun deleteEntireIndex(indexType: IndexType<*>)
    fun refresh(indexType: IndexType<*>)
    fun indexHealth(indexName: String): Health
    fun indexHealthChecker(indexName: String): Pair<URI, () -> Health>

}
