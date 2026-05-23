package ksqlite.capi.memory

import co.touchlab.stately.concurrency.close
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
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
     */
    private inline fun <C, D : AutoDisposable<C>> commonRegisterDisposable(
        factory: (id: Long) -> D,
        computeId: () -> Long
    ): D = disposableLock.withLock {
        val disposableId = computeId()
        val newDisposable = factory(disposableId)

        disposables
            .put(disposableId, newDisposable)
            ?.destroy() // Dispose previous disposable with the same key

        return newDisposable
    }

    /**
     * Registers and returns a disposable [D].
     *
     * The registered disposable is disposed on call to [clear] if it was not manually disposed.
     */
    protected fun <C, D : AutoDisposable<C>> registerDisposable(
        factory: (id: Long) -> D
    ): D = commonRegisterDisposable(factory, ::computeNextDisposableId)

    /**
     * Registers and returns a disposable [D] identified by [key], disposing any disposable
     * previously registered with the same [key].
     *
     * The registered disposable is disposed on call to [clear] if it was not manually disposed.
     */
    protected fun <C, D : AutoDisposable<C>> registerKeyedDisposable(
        key: String,
        factory: (id: Long) -> D
    ): D = commonRegisterDisposable(factory) {
        keyedDisposables.getOrPut(key, ::computeNextDisposableId)
    }

    /**
     * [Disposable] which can self removes from [disposables].
     * [destroy] should be used to make the actual disposing.
     */
    protected abstract inner class AutoDisposable<AppData>(
        private val id: Long,
        private val destructor: Sqlite3DestroyCallback<AppData>?
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
            destructor?.handle(appData)
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