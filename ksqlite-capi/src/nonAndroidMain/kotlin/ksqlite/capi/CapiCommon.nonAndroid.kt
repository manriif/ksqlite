package ksqlite.capi

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.memoryOrNull
import ksqlite.types.SqliteDataType
import ksqlite.types.SqliteResultCode
import ksqlite.types.internal.convertResultCode

/**
 * Returns the [sqlite3] associated with `this` [sqlite3_context].
 */
internal val sqlite3_context.db: sqlite3
    get() = sqlite3_context_db_handle(this)

///////////////////////////////////////////////////////////////////////////
// Memory
///////////////////////////////////////////////////////////////////////////

/**
 * Handles the [ksqlite.capi.sqlite3_clear_bindings].
 */
internal fun commonClearBindings(stmt: sqlite3_stmt, result: Int): SqliteResultCode {
    if (result == SqliteResultCode.OK.code) {
        stmt.memoryOrNull?.let { manager ->
            check(manager.isEmpty) { "Statement disposables has not been disposed all" }
        }
    }

    return convertResultCode(result)
}

///////////////////////////////////////////////////////////////////////////
// Blob
///////////////////////////////////////////////////////////////////////////

internal val EmptyByteArray = ByteArray(0)

/**
 * Common code for getting a [ByteArray] or a [Buffer].
 */
private inline fun <Pointer : Any, Blob : Any> commonGetBlob(
    pointer: Pointer?,
    emptyBlob: Blob,
    toBlob: (pointer: Pointer, size: Int) -> Blob?,
    getSize: () -> Int,
    getType: () -> SqliteDataType
): Blob? {
    if (pointer == null) {
        return null
    }

    val size = getSize()
    check(size >= 0)

    return if (size == 0) {
        when (val type = getType()) {
            BLOB -> emptyBlob
            NULL -> null
            else -> error(
                "Expected a value of type ${SqliteDataType.BLOB} but actual value is of type $type"
            )
        }
    } else {
        toBlob(pointer, size)
    }
}

/**
 * Common code for [commonColumnBuffer] and [commonValueBuffer].
 */
private inline fun <Pointer : Any> commonGetBuffer(
    pointer: Pointer?,
    toBuffer: (pointer: Pointer, size: Long) -> Buffer?,
    getSize: () -> Int,
    getType: () -> SqliteDataType
): Buffer? = commonGetBlob(
    pointer = pointer,
    emptyBlob = Buffer.Empty,
    toBlob = { pointer, size -> toBuffer(pointer, size.toLong()) },
    getSize = getSize,
    getType = getType
)

/**
 * Common code for [commonColumnByteArray] and [commonValueByteArray].
 */
private inline fun <Pointer : Any> commonGetByteArray(
    pointer: Pointer?,
    toByteArray: (pointer: Pointer, size: Int) -> ByteArray,
    getSize: () -> Int,
    getType: () -> SqliteDataType
): ByteArray? = commonGetBlob(
    pointer = pointer,
    emptyBlob = EmptyByteArray,
    toBlob = toByteArray,
    getSize = getSize,
    getType = getType
)

/**
 * Handles the [ksqlite.capi.columnBufferInternal].
 */
internal inline fun <Pointer : Any> commonColumnBuffer(
    stmt: sqlite3_stmt,
    index: Int,
    pointer: Pointer?,
    toBuffer: (pointer: Pointer, size: Long) -> Buffer?
): Buffer? = commonGetBuffer(
    pointer = pointer,
    toBuffer = toBuffer,
    getSize = { sqlite3_column_bytes(stmt, index) },
    getType = { sqlite3_column_type(stmt, index) }
)

/**
 * Handles the [ksqlite.capi.sqlite3_column_blob].
 */
internal inline fun <Pointer : Any> commonColumnByteArray(
    stmt: sqlite3_stmt,
    index: Int,
    pointer: Pointer?,
    toByteArray: (pointer: Pointer, size: Int) -> ByteArray
): ByteArray? = commonGetByteArray(
    pointer = pointer,
    toByteArray = toByteArray,
    getSize = { sqlite3_column_bytes(stmt, index) },
    getType = { sqlite3_column_type(stmt, index) }
)

/**
 * Handles the [ksqlite.capi.valueBufferInternal].
 */
internal inline fun <Pointer : Any> commonValueBuffer(
    value: sqlite3_value,
    pointer: Pointer?,
    toBuffer: (pointer: Pointer, size: Long) -> Buffer?
): Buffer? = commonGetBuffer(
    pointer = pointer,
    toBuffer = toBuffer,
    getSize = { sqlite3_value_bytes(value) },
    getType = { sqlite3_value_type(value) }
)

/**
 * Handles the [ksqlite.capi.sqlite3_value_blob].
 */
internal inline fun <Pointer : Any> commonValueByteArray(
    value: sqlite3_value,
    pointer: Pointer?,
    toByteArray: (pointer: Pointer, size: Int) -> ByteArray
): ByteArray? = commonGetByteArray(
    pointer = pointer,
    toByteArray = toByteArray,
    getSize = { sqlite3_value_bytes(value) },
    getType = { sqlite3_value_type(value) }
)