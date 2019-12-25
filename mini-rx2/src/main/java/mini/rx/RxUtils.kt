package mini.rx

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mini.Store

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Flowable<T>.mapNotNull(crossinline fn: (T) -> U?): Flowable<U> {
    return filter { fn(it) != null }.map { fn(it) }
}

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Observable<T>.mapNotNull(crossinline fn: (T) -> U?): Observable<U> {
    return filter { fn(it) != null }.map { fn(it) }
}

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Flowable<T>.select(crossinline fn: (T) -> U?): Flowable<U> {
    return mapNotNull(fn).distinctUntilChanged()
}

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Observable<T>.select(crossinline fn: (T) -> U?): Observable<U> {
    return mapNotNull(fn).distinctUntilChanged()
}

interface SubscriptionTracker {
    /**
     * Clear Subscriptions.
     */
    fun clearSubscriptions()

    /**
     * Start tracking a disposable.
     */
    fun <T : Disposable> T.track(): T
}

class DefaultSubscriptionTracker : SubscriptionTracker {
    private val disposables = CompositeDisposable()
    override fun clearSubscriptions() = disposables.clear()
    override fun <T : Disposable> T.track(): T {
        disposables.add(this)
        return this
    }
}

fun <S> Store<S>.flowable(hotStart: Boolean = true): Flowable<S> {
    val processor = PublishProcessor.create<S>()

    CoroutineScope(Dispatchers.Main).launch {
        flow().collect {
            processor.offer(it)
        }
    }

    return processor.let { if (hotStart) it.startWith(state) else it }
}

fun <S> Store<S>.observable(hotStart: Boolean = true): Observable<S> {
    val subject = PublishSubject.create<S>()

    CoroutineScope(Dispatchers.Main).launch {
        flow().collect {
            subject.onNext(it)
        }
    }

    return subject.let { if (hotStart) it.startWith(state) else it }
}