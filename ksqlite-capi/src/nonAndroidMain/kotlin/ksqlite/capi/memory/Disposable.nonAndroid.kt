package ksqlite.capi.memory

import ksqlite.capi.callbacks.SqliteDestroyCallback

/**
 * Resource which can be disposed.
 */
internal interface Disposable {

    /**
     * Disposes the resource.
     */
    fun dispose()
}

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/**
 * Holds any [Disposable] that should be reachable by static C function given a pointer.
 */
private val GlobalDisposables by lazy { ConcurrentMap<Long, Disposable>() }

/**
 * Called from platform global disposer after a native function called a xDestroy hook.
 */
internal fun disposeGlobal(address: Long?) {
    checkNotNull(address) { "Address must not be null for a disposable registered globally" }
    checkNotNull(GlobalDisposables[address]).dispose()

    // It is the owner responsibility to unregister the disposable after dispose have been called
    check(GlobalDisposables[address] == null)
}

/**
 * Registers [disposable] associated with [address].
 *
 * The owner of the [Disposable] must call [unregisterGlobalDisposable] when [Disposable.dispose]
 * is invoked.
 */
internal fun registerGlobalDisposable(
    address: Long,
    disposable: Disposable
) {
    check(GlobalDisposables.put(address, disposable) == null) {
        "A disposable is already registered for the pointed address"
    }
}

/**
 * Unregisters a previously registered [Disposable] associated with [address].
 */
internal fun unregisterGlobalDisposable(address: Long) {
    check(GlobalDisposables.remove(address) != null) {
        "No disposable was registered fo the pointed address"
    }
}

///////////////////////////////////////////////////////////////////////////
// Wrapper
///////////////////////////////////////////////////////////////////////////

/**
 * [Disposable] invoking [destructor] with [instance] when disposed.
 */
internal class InstanceDestructor<Instance>(
    private val address: Long,
    private val instance: Instance,
    private val destructor: SqliteDestroyCallback<Instance>
) : Disposable {

    override fun dispose() {
        unregisterGlobalDisposable(address)
        destructor.apply(instance)
    }
}

/**
 * Registers a [Disposable] that will invoke [destructor] when disposed.
 *
 * If [destructor] == `null` then `null` is returned and no [Disposable] is registered. Otherwise,
 * [disposer] is returned.
 *
 * This API is a fallback when [MemoryManager] cannot be used because a native function expects a
 * direct pointer to a contiguous region of bytes and the application supplies a [destructor].
 */
internal fun <Instance, Disposer : Any> instanceDisposer(
    disposer: Disposer,
    instance: Instance,
    address: Long,
    destructor: SqliteDestroyCallback<Instance>?,
): Disposer? {
    if (destructor == null) {
        return null
    }

    registerGlobalDisposable(
        address = address,
        disposable = InstanceDestructor(
            address = address,
            instance = instance,
            destructor = destructor
        )
    )

    return disposer
}