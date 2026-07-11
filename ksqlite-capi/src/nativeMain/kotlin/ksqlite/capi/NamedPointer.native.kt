package ksqlite.capi

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.cstr
import ksqlite.capi.callbacks.SqliteDestroyCallback

/**
 * Wrapper for [sqlite3_bind_pointer] and [sqlite3_result_pointer] data.
 */
internal class NamedPointer<Data>(
    val name: CPointer<ByteVar>?,
    private val arena: Arena?,
    private val destroy: SqliteDestroyCallback<Data>?
) {

    /**
     * Invokes application [destroy] and clears the associated [arena].
     */
    fun destroy(data: Data) {
        destroy?.apply(data)
        arena?.clear()
    }
}

/**
 * Returns a [NamedPointer] which allocates memory for [type] if not null.
 *
 * The destructor passed to [block] must be used in place of [destroy] in order to clear the
 * associated [Arena].
 */
internal inline fun <Data, R> allocateNamedPointer(
    type: String?,
    destroy: SqliteDestroyCallback<Data>?,
    block: (
        ptr: NamedPointer<Data>,
        ptrDestroy: SqliteDestroyCallback<Data>
    ) -> R
): R {
    val arena = type?.let { Arena() }
    val typePointer = arena?.let(type.cstr::getPointer)

    val pointer = NamedPointer(
        name = typePointer,
        arena = arena,
        destroy = destroy
    )

    return block(pointer) { pointer.destroy(it) }
}