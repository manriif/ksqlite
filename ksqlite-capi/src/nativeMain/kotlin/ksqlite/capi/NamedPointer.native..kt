package ksqlite.capi

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.cstr
import ksqlite.capi.types.Sqlite3DestructorCallback

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] userData.
 */
internal class NamedPointer(
    val typePointer: CPointer<ByteVar>?,
    private val arena: Arena?,
    private val userDestructor: Sqlite3DestructorCallback?
) {
    /**
     * Destructor replacing original user provided destructor.
     */
    val disposer: Sqlite3DestructorCallback = { userData ->
        userDestructor?.invoke(userData)
        arena?.clear()
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
    val arena = type?.let { Arena() }
    val typePointer = arena?.let(type.cstr::getPointer)

    return block(
        NamedPointer(
            typePointer = typePointer,
            arena = arena,
            userDestructor = destructor
        )
    )
}