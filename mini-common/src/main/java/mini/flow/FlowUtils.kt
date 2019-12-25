package mini.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import mini.Store

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged].
 */
@FlowPreview
fun <T, R> Flow<T>.select(mapper: suspend (T) -> R): Flow<R> {
    return this
            .map { mapper(it) }
            .distinctUntilChanged()
}

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged] ignoring null values.
 */
@FlowPreview
fun <T, R : Any> Flow<T>.selectNotNull(mapper: suspend (T) -> R?): Flow<R> {
    return this
            .map { mapper(it) }
            .filterNotNull()
            .distinctUntilChanged()
}