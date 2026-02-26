package ksqlite.capi.memory

import ksqlite.capi.convertResult
import ksqlite.capi.types.Sqlite3Result

/**
 * [Pointer] with a [MemoryManager] sharing the same lifecycle as the [pointer].
 */
public abstract class MemoryPointer<Pointer : Any> internal constructor(
    internal val pointer: Pointer,
    restricted: Boolean
) {

    private var _memory = lazy {
        if (restricted) {
            throw UnsupportedOperationException(
                "This pointer is restricted and some sqlite APIs cannot be called because of not " +
                        "yet resolved memory management issues."
            )
        }

        MemoryManager()
    }

    internal val memory by _memory

    /**
     * Invokes [block] which is expected to be the SQLite function that will deallocate [pointer]
     * and returns [block]'s result.
     *
     * If the deallocation succeeds, which is the case if [block] returns [Sqlite3Result.OK], then
     * all the resources associated with [pointer] through [memory] are disposed and [memory] is
     * closed before the function returns.
     */
    internal inline fun deallocate(block: (Pointer) -> Int): Sqlite3Result {
        val result = convertResult(block(pointer))

        if (result == Sqlite3Result.OK) {
            if (_memory.isInitialized()) {
                memory.close()
            }
        }

        return result
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [MemoryPointer.deallocate] if `this` is not `null`. Otherwise, returns [block]'s result
 * passing it a `null` [Pointer].
 *
 * It is expected for [block] to be the SQLite function that will deallocate [Pointer].
 */
internal inline fun <Pointer : Any> MemoryPointer<Pointer>?.deallocate(
    block: (Pointer?) -> Int
): Sqlite3Result {
    if (this == null) {
        return convertResult(block(null))
    }

    return this.deallocate(block)
}