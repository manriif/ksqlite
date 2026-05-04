package ksqlite.capi

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memoryOrNull
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3VirtualTableConfigOption
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_stmt
import kotlin.jvm.JvmInline

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [sqlite3] associated with `this` [sqlite3_context].
 */
internal val sqlite3_context.db: sqlite3
    get() = TODO()/*checkNotNull(sqlite3_context_db_handle(this)) {
        "Database pointer not retrieved from context"
    }*/

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

    @JvmInline
    value class OfString(override val value: String) : VariadicValue<Nothing>
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * Handles the [ksqlite.capi.sqlite3_clear_bindings].
 */
internal fun commonSqlite3ClearBindings(stmt: sqlite3_stmt, result: Int): Sqlite3Result {
    if (result == Sqlite3Result.OK.code) {
        // Release memory allocated for binded values, although destructors have normally already
        // been called by previous call to native_sqlite3_clear_bindings
        stmt.memoryOrNull?.clear()
    }

    return convertResult(result)
}

/**
 * Handles the [ksqlite.capi.sqlite3_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
internal fun <Pointer : Any> commonSqlite3Config(
    option: Sqlite3ConfigOption,
    memoryPointer: (sqlite3_pointer) -> Pointer?,
    logFunctionPointer: (callback: Any?) -> Pointer?,
    sqllogFunctionPointer: (callback: Any?) -> Pointer?,
    keyedStableRefPointer: MemoryManager.(String, Any?, sqlite3_mutable_pointer?) -> Pointer?,
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
                pMem?.let(memoryPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(nBytes),
                VariadicValue.OfInt(min)
            )

            is LOG -> arrayOf(
                logFunctionPointer(callback)?.let(VariadicValue<Pointer>::OfPointer),
                globalMemory.keyedStableRefPointer(KEY_CONFIG_LOG, callback, userData)
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
                pMem?.let(memoryPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(sz),
                VariadicValue.OfInt(n)
            )

            is PMASZ -> arrayOf(VariadicValue.OfUInt(szPma))
            is SMALL_MALLOC -> arrayOf(VariadicValue.OfInt(enabled))
            is SORTERREF_SIZE -> arrayOf(VariadicValue.OfInt(nByte))

            is SQLLOG -> arrayOf(
                sqllogFunctionPointer(callback)?.let(VariadicValue<Pointer>::OfPointer),
                globalMemory.keyedStableRefPointer(KEY_CONFIG_SQLLOG, callback, userData)
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
internal fun <Pointer : Any> commonSqlite3DbConfig(
    option: Sqlite3DbConfigOption,
    memoryPointer: (sqlite3_pointer) -> Pointer?,
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
                buf?.let(memoryPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(sz),
                VariadicValue.OfInt(cnt)
            )

            is MAINDBNAME -> arrayOf(VariadicValue.OfString(name))
        }
    }

    return convertResult(nativeConfig(option.id, args))
}

/**
 * Handles the [ksqlite.capi.sqlite3_vtab_config].
 * The array passed to [nativeConfig] contains at most 1 value.
 */
internal fun <Pointer : Any> commonSqlite3VtabConfig(
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