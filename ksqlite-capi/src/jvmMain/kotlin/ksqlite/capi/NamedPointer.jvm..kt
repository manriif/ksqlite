package ksqlite.capi

import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.utils.allocateUtf8
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] userData.
 */
internal class NamedPointer(
    val typePointer: MemorySegment?,
    private val arena: Arena?,
    private val destructor: Sqlite3DestructorCallback?
) {
    /**
     * Destructor replacing original user provided destructor.
     */
    val disposer: Sqlite3DestructorCallback = { userData ->
        destructor?.invoke(userData)
        arena?.close()
    }
}

/**
 * Returns a [NamedPointer] which allocates memory for [type] if not null.
 *
 * The returned [NamedPointer.disposer] must be used in place of [destructor] in order to clear
 * the associated [Arena].
 */
internal inline fun <R> allocateNamedPointer(
    type: String?,
    noinline destructor: Sqlite3DestructorCallback?,
    block: NamedPointer.() -> R
): R {
    val arena = type?.let { Arena.ofShared() }
    val typePointer = arena?.run { type.allocateUtf8() }

    return block(
        NamedPointer(
            typePointer = typePointer,
            arena = arena,
            destructor = destructor
        )
    )
}