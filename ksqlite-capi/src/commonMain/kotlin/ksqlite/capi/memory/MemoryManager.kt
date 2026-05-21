package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import kotlin.concurrent.Volatile

/**
 * Manages memory.
 */
internal abstract class MemoryManagerBase : AutoCloseable {

    private val disposables: MutableMap<ULong, AutoDisposable<*>> by lazy(::mutableMapOf)
    private val keyedIds: MutableMap<String, ULong> by lazy(::mutableMapOf)

    @Volatile
    private var nextId = 0UL

    @Volatile
    private var closed = false

    ///////////////////////////////////////////////////////////////////////////
    // Clearing
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Releases all the resources but keep the manager alive.
     * Parent function must be called.
     */
    open fun clear() {
        if (disposables.isNotEmpty()) {
            disposables.onEach { it.value.destroy() }.clear()
        }

        if (keyedIds.isNotEmpty()) {
            keyedIds.clear()
        }
    }

    /**
     * Invokes and returns [block]'s result throwing an [IllegalStateException] if this instance is
     * closed.
     */
    protected inline fun <T> notClosed(block: () -> T): T {
        check(!closed) { "Manager is closed" }
        return block()
    }

    final override fun close() {
        if (!closed) {
            closed = true
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
    protected inline fun <ClientData, reified D : AutoDisposable<ClientData>> getDisposable(
        id: ULong
    ): D {
        val disposable = disposables[id]

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
     * Registers and returns a disposable [D] that should be disposed on [clear].
     *
     * If [key] is not `null` then any previously registered disposable with the same key is
     * disposed.
     */
    protected fun <ClientData, D : AutoDisposable<ClientData>> registerDisposable(
        key: String? = null,
        block: (id: ULong) -> D
    ): D {
        val disposableId = key?.let(keyedIds::get) ?: (++nextId).also { id ->
            check(id > 0UL) { "Too many disposable were created (>${ULong.MAX_VALUE})" }
            key?.let { keyedIds[it] = id }
        }

        val disposable = block(disposableId)

        disposables
            .put(disposableId, disposable)
            ?.destroy() // Dispose previous disposable with the same key

        return disposable
    }

    /**
     * [Disposable] which can self removes from [disposables].
     * [destroy] should be used to make the actual disposing.
     */
    protected abstract inner class AutoDisposable<ClientData>(
        private val id: ULong,
        private val destructor: Sqlite3DestructorCallback<ClientData>?
    ) : Disposable {

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
            val instance = checkNotNull(disposables.remove(id)) {
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