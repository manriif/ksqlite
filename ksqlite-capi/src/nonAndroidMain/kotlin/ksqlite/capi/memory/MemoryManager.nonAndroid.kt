package ksqlite.capi.memory

import co.touchlab.stately.concurrency.close
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.callbacks.SqliteDestroyCallback
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Base for [MemoryManager].
 */
@OptIn(ExperimentalAtomicApi::class)
internal abstract class MemoryManagerBase : AutoCloseable {

    // Accessing the three next variable require synchronization
    private val disposables = mutableMapOf<Long, AutoDisposable<*>>()
    private val keyedDisposables = mutableMapOf<String, Long>()
    private var nextDisposableId = 0L
    private val disposableLock = Lock()

    private val closed = AtomicBoolean(false)

    internal val isEmpty: Boolean
        get() = disposableLock.withLock { disposables.isEmpty() }

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
            disposableLock.close()
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
        disposableLock.lock()
        val nextId = ++nextDisposableId

        check(nextId > 0L) {
            "Too many disposables were created (>${Long.MAX_VALUE})"
        }

        return nextId
    }

    /**
     * Registers and returns a disposable [D].
     *
     * If [key] is supplied, then any disposable previously identified with the same key is
     * disposed.
     *
     * The registered disposable is disposed on call to [clear] if it was not manually disposed.
     */
    protected fun <C, D : AutoDisposable<C>> registerDisposable(
        key: String? = null,
        factory: (id: Long) -> D
    ): D = disposableLock.withLock {
        val disposableId = key
            ?.let { keyedDisposables.getOrPut(it, ::computeNextDisposableId) }
            ?: computeNextDisposableId()

        val disposable = factory(disposableId).apply {
            disposableKey = key
        }

        disposables
            .put(disposableId, disposable)
            ?.destroy() // Dispose previous disposable with the same key

        return disposable
    }

    /**
     * Clears the disposable associated with [key] if any and if [key] is not `null`.
     */
    fun clearDisposable(key: String) {
        val disposable = disposableLock.withLock {
            keyedDisposables[key]?.let(disposables::get)
        }

        disposable?.dispose()
    }

    /**
     * [Disposable] which can self removes from [disposables].
     * [destroy] should be used to make the actual disposing.
     */
    protected abstract inner class AutoDisposable<AppData>(
        private val id: Long,
        private val destructor: SqliteDestroyCallback<AppData>?
    ) : Disposable {

        var disposableKey: String? = null

        /**
         * The associated client data.
         * */
        abstract val appData: AppData

        /**
         * Releases the resource(s).
         */
        abstract fun release()

        /**
         * Invokes destructor and releases the resource(s).
         */
        fun destroy() {
            destructor?.apply(appData)
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
 * Manages memory.
 *
 * Provides helper functions to obtain native pointers to long-lived object and ensuring no object
 * leak.
 *
 * Note that this is meaningless on Android, but declaring it here reduce the source code
 * complexity.
 */
internal expect class MemoryManager() : MemoryManagerBase