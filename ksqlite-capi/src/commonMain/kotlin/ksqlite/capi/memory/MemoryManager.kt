package ksqlite.capi.memory

import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Manages memory.
 */
@OptIn(ExperimentalAtomicApi::class)
internal abstract class MemoryManagerBase : AutoCloseable {

    private val disposables: MutableMap<Long, AutoDisposable<*>> by lazy(::mutableMapOf)
    private val keyedDisposables: MutableMap<String, Long> by lazy(::mutableMapOf)
    private var nextDisposableId = 0L

    // Lock for all previous variable
    private val disposableLock = Lock()

    private val closed = AtomicBoolean(false)

    ///////////////////////////////////////////////////////////////////////////
    // Clearing
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Releases all the resources but keep the manager alive.
     * Parent function must be called.
     */
    open fun clear() = disposableLock.withLock {
        if (disposables.isNotEmpty()) {
            disposables.onEach { it.value.destroy() }.clear()
        }

        if (keyedDisposables.isNotEmpty()) {
            keyedDisposables.clear()
        }
    }

    /**
     * Invokes and returns [block]'s result throwing an [IllegalStateException] if this instance is
     * closed.
     */
    protected inline fun <T> notClosed(block: () -> T): T {
        check(!closed.load()) { "Manager is closed" }
        return block()
    }

    final override fun close() {
        if (closed.compareAndSet(expectedValue = false, newValue = true)) {
            clear()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposables
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a previously registered disposable identified by [id].
     *
     * @throws NullPointerException if no object is associated with [id].
     */
    protected inline fun <C, reified D : AutoDisposable<C>> getDisposable(id: Long): D {
        val disposable = disposableLock.withLock {
            disposables[id]
        }

        if (disposable != null) {
            if (disposable !is D) {
                throw ClassCastException(
                    "Disposable expected to be of type ${D::class} but actual type is " +
                            "${disposable::class}"
                )
            }

            return disposable
        }

        throw NullPointerException(
            "No disposable is associated with id $id or it has been disposed"
        )
    }

    /**
     * Returns the next available disposable identifier.
     */
    private fun computeNextDisposableId(): Long {
        val nextId = ++nextDisposableId

        check(nextId > 0L) {
            "Too many disposables were created (>${Long.MAX_VALUE})"
        }

        return nextId
    }

    /**
     * Registers and returns a disposable [D].
     * The registered disposable is disposed on call to [clear] if it was not manually disposed.
     */
    protected fun <C, D : AutoDisposable<C>> registerDisposable(
        factory: (id: Long) -> D
    ): D = disposableLock.withLock {
        val disposableId = computeNextDisposableId()
        val newDisposable = factory(disposableId)

        disposables
            .put(disposableId, newDisposable)
            ?.destroy() // Dispose previous disposable with the same key

        return newDisposable
    }

    /**
     * Registers a disposable [D] identified by [key], disposing any disposable previously
     * registered with the same [key], and returns both disposables.
     *
     * The registered disposable is disposed on call to [clear] if it was not manually disposed.
     */
    protected fun <C, D : AutoDisposable<C>> registerKeyedDisposable(
        key: String,
        factory: (id: Long) -> D
    ): KeyedDisposable<D> = disposableLock.withLock {
        val disposableId = keyedDisposables.getOrPut(key, ::computeNextDisposableId)

        val newDisposable = factory(disposableId).apply {
            disposableKey = key
        }

        val oldDisposable = disposables.put(disposableId, newDisposable)?.apply {
            destroy() // Dispose previous disposable with the same key
        }

        return KeyedDisposable(newDisposable, oldDisposable?.clientData)
    }

    /**
     * Holder for a disposable and oldly registered client data.
     */
    protected data class KeyedDisposable<D>(val disposable: D, val oldClientData: Any?)

    /**
     * [Disposable] which can self removes from [disposables].
     * [destroy] should be used to make the actual disposing.
     */
    protected abstract inner class AutoDisposable<ClientData>(
        private val id: Long,
        private val destructor: Sqlite3DestructorCallback<ClientData>?
    ) : Disposable {

        var disposableKey: String? = null

        /**
         * The associated client data.
         * */
        abstract val clientData: ClientData

        /**
         * Releases the resource(s).
         */
        abstract fun release()

        /**
         * Invokes destructor and releases the resource(s).
         */
        fun destroy() {
            destructor?.handle(clientData)
            release()
        }

        /**
         * Removes `this` from the [disposables] and [destroy] the instance.
         */
        final override fun dispose() {
            val instance = checkNotNull(disposableLock.withLock {
                disposables.remove(
                    when (val key = disposableKey) {
                        null -> id
                        else -> when (val keyId = keyedDisposables.remove(key)) {
                            id -> keyId
                            null -> error("Keyed disposable returned null identifier")
                            else -> error("Keyed disposable returned wrong identifier")
                        }
                    }
                )
            }) {
                "Resource is no longer managed"
            }

            check(instance === this) { "Unexpected disposable instance" }
            destroy()
        }
    }
}

/**
 * Platform specific memory manager.
 */
internal expect class MemoryManager() : MemoryManagerBase