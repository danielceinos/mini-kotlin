package mini

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onStart
import org.jetbrains.annotations.TestOnly
import java.io.Closeable
import java.lang.reflect.ParameterizedType

/**
 * State holder.
 */
abstract class Store<S> : Closeable {

    companion object {
        val NO_STATE = Any()
    }

    private var _state: Any? = NO_STATE
    private val channel = BroadcastChannel<S>(Channel.BUFFERED)

    /** Set new state, equivalent to [asNewState]*/
    protected fun setState(state: S) {
        assertOnUiThread()
        performStateChange(state)
    }

    /** Hook for write only property */
    protected var newState: S
        get() = throw UnsupportedOperationException("This is a write only property")
        set(value) = setState(value)

    /** Same as property, suffix style */
    protected fun S.asNewState(): S {
        assertOnUiThread()
        performStateChange(this)
        return this
    }

    fun flow(hotStart: Boolean = true): Flow<S> {
        return channel.asFlow().onStart { if (hotStart) emit(state) }
    }

    val state: S
        get() {
            if (_state === NO_STATE) {
                synchronized(this) {
                    if (_state === NO_STATE) {
                        _state = initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }

    /**
     * Initialize the store after dependency injection is complete.
     */
    open fun initialize() {
        //No-op
    }

    @Suppress("UNCHECKED_CAST")
    open fun initialState(): S {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                as Class<S>
        try {
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state $type", e)
        }
    }

    private fun performStateChange(newState: S) {
        //State mutation should to happen on UI thread
        if (newState != _state) {
            _state = newState
            channel.offer(newState)
        }
    }

    /** Test only method, don't use in app code */
    @TestOnly
    fun setTestState(s: S) {
        if (isAndroid) {
            onUiSync {
                performStateChange(s)
            }
        } else {
            performStateChange(s)
        }
    }

    /** Set state back to initial default */
    @TestOnly
    fun resetState() {
        setTestState(initialState())
    }

    final override fun close() {
        channel.close()
        onClose()
    }

    open fun onClose() = Unit
}
