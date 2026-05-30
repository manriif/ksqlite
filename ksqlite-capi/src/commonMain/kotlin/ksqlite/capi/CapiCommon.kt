package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3ConfigLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memoryOrNull
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3VirtualTableConfigOption
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import kotlin.jvm.JvmInline

/**
 * Returns the [sqlite3] associated with `this` [sqlite3_context].
 */
internal val sqlite3_context.db: sqlite3
    get() = checkNotNull(sqlite3_context_db_handle(this)) {
        "Database pointer not retrieved from context"
    }

///////////////////////////////////////////////////////////////////////////
// Memory
///////////////////////////////////////////////////////////////////////////

/**
 * Handles the [ksqlite.capi.sqlite3_clear_bindings].
 */
internal fun commonClearBindings(stmt: sqlite3_stmt, result: Int): Sqlite3Result {
    if (result == Sqlite3Result.OK.code) {
        // Release memory allocated for binded values, although destructors have normally already
        // been called by previous call to native_sqlite3_clear_bindings
        stmt.memoryOrNull?.clear()
    }

    return convertResult(result)
}

///////////////////////////////////////////////////////////////////////////
// Blob
///////////////////////////////////////////////////////////////////////////

internal val EmptyByteArray = ByteArray(0)

/**
 * Common code for getting a [ByteArray] or a [Buffer].
 */
private inline fun <Pointer : Any, Blob: Any> commonGetBlob(
    pointer: Pointer?,
    emptyBlob: Blob,
    toBlob: (pointer: Pointer, size: Int) -> Blob?,
    getSize: () -> Int,
    getType: () -> Sqlite3DataType
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
                "Expected a value of type ${Sqlite3DataType.BLOB} but actual value is of type $type"
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
    getType: () -> Sqlite3DataType
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
    getType: () -> Sqlite3DataType
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

///////////////////////////////////////////////////////////////////////////
// Configuration
///////////////////////////////////////////////////////////////////////////

/**
 * Value of a variadic function call.
 */
internal sealed interface VariadicValue<out Pointer : Any> {

    val value: Any

    @JvmInline
    value class OfPointer<P : Any>(override val value: P) : VariadicValue<P>

    @JvmInline
    value class OfInt(override val value: Int) : VariadicValue<Nothing>

    @JvmInline
    value class OfUInt(override val value: UInt) : VariadicValue<Nothing>

    @JvmInline
    value class OfLong(override val value: Long) : VariadicValue<Nothing>

    /**
     * Strings are given a key because it is possible that the application must manage it as SQLite
     * won't make a copy of it.
     *
     * For now, [Sqlite3DbConfigOption.MAINDBNAME] is the only string that must stay alive until the
     * database connection is closed.
     */
    @JvmInline
    data class OfString(override val value: String, val key: String) : VariadicValue<Nothing>
}

/**
 * Handles the [ksqlite.capi.sqlite3_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
@Suppress("UNCHECKED_CAST")
internal fun <Pointer : Any> commonConfig(
    option: Sqlite3ConfigOption,
    bufferPointer: (Buffer) -> Pointer?,
    logFunctionPointer: (callback: Sqlite3ConfigLogCallback<Any?>?, appData: Any?) -> Pointer?,
    sqllogFunctionPointer: (callback: Sqlite3ConfigSqlLogCallback<Any?>?, appData: Any?) -> Pointer?,
    keyedStableRefPointer: (MemoryManager.(String, Any?, Any?) -> Pointer?)?,
    rowidInView: Sqlite3ConfigOption.ROWID_IN_VIEW.() -> Int,
    nativeConfig: (id: Int, args: Array<out VariadicValue<Pointer>?>) -> Int,
): Sqlite3Result {
    val args = with(option) {
        if (this is ROWID_IN_VIEW) {
            return convertResult(rowidInView())
        }

        when (this) {
            SERIALIZED, MULTITHREAD, SINGLETHREAD -> emptyArray<VariadicValue<Pointer>>()
            is COVERING_INDEX_SCAN -> arrayOf(VariadicValue.OfInt(enabled))

            is HEAP -> arrayOf(
                pMem?.let(bufferPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(nBytes),
                VariadicValue.OfInt(min)
            )

            is LOG<*> -> arrayOf(
                logFunctionPointer(callback as Sqlite3ConfigLogCallback<Any?>?, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer),
                keyedStableRefPointer
                    ?.invoke(globalMemory, KEY_CONFIG_LOG, callback, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer)
            )

            is LOOKASIDE -> arrayOf(
                VariadicValue.OfInt(sz),
                VariadicValue.OfInt(cnt)
            )

            is MEMDB_MAXSIZE -> arrayOf(VariadicValue.OfLong(maxSize))
            is MEMSTATUS -> arrayOf(VariadicValue.OfInt(enabled))

            is MMAP_SIZE -> arrayOf(
                VariadicValue.OfLong(sz),
                VariadicValue.OfLong(mx)
            )

            is PAGECACHE -> arrayOf(
                pMem?.let(bufferPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(sz),
                VariadicValue.OfInt(n)
            )

            is PMASZ -> arrayOf(VariadicValue.OfUInt(szPma))
            is SMALL_MALLOC -> arrayOf(VariadicValue.OfInt(enabled))
            is SORTERREF_SIZE -> arrayOf(VariadicValue.OfInt(nByte))

            is SQLLOG<*> -> arrayOf(
                sqllogFunctionPointer(callback as Sqlite3ConfigSqlLogCallback<Any?>?, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer),
                keyedStableRefPointer
                    ?.invoke(globalMemory, KEY_CONFIG_SQLLOG, callback, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer)
            )

            is STMTJRNL_SPILL -> arrayOf(VariadicValue.OfInt(nByte))
            is URI -> arrayOf(VariadicValue.OfInt(value))
            is WIN32_HEAPSIZE -> arrayOf(VariadicValue.OfUInt(nByte))
        }
    }

    return convertResult(nativeConfig(option.id, args))
}

/**
 * Handles the [ksqlite.capi.sqlite3_db_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
internal fun <Pointer : Any> commonDbConfig(
    option: Sqlite3DbConfigOption,
    bufferPointer: (Buffer) -> Pointer?,
    outParamConfig: Sqlite3DbConfigOption.IntOutput.() -> Int,
    nativeConfig: (id: Int, values: Array<out VariadicValue<Pointer>?>) -> Int,
): Sqlite3Result {
    val args = with(option) {
        when (this) {
            is IntOutput -> {
                if (state != null) {
                    return convertResult(outParamConfig())
                }

                arrayOf(VariadicValue.OfInt(value), null)
            }

            is LOOKASIDE -> arrayOf(
                buf?.let(bufferPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(sz),
                VariadicValue.OfInt(cnt)
            )

            is MAINDBNAME -> arrayOf(VariadicValue.OfString(name, KEY_DB_CONFIG_MAINDBNAME))
        }
    }

    return convertResult(nativeConfig(option.id, args))
}

/**
 * Handles the [ksqlite.capi.sqlite3_vtab_config].
 * The array passed to [nativeConfig] contains at most 1 value.
 */
internal fun <Pointer : Any> commonVtabConfig(
    option: Sqlite3VirtualTableConfigOption,
    nativeConfig: (id: Int, values: Array<out VariadicValue<Pointer>?>) -> Int,
): Sqlite3Result {
    val args = with(option) {
        when (this) {
            is CONSTRAINT_SUPPORT -> arrayOf(VariadicValue.OfInt(enabled))
            DIRECTONLY, INNOCUOUS, USES_ALL_SCHEMAS -> emptyArray()
        }
    }

    return convertResult(nativeConfig(option.id, args))
}