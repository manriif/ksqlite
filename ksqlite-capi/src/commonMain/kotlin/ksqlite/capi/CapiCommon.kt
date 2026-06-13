package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3ConfigLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.vtab.Sqlite3VTabConfigOption
import kotlin.jvm.JvmInline

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
    data class OfString(override val value: String, val key: String) : VariadicValue<Nothing>
}

///////////////////////////////////////////////////////////////////////////
// Configuration
///////////////////////////////////////////////////////////////////////////

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
    keyedStableRefPointer: ((String, Any?, Any?) -> Pointer?)?,
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
                    ?.invoke(KEY_CONFIG_LOG, callback, appData)
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

            is PCACHE_HDRSZ -> arrayOf(VariadicValue.OfInt(psz))
            is PMASZ -> arrayOf(VariadicValue.OfUInt(szPma))
            is SMALL_MALLOC -> arrayOf(VariadicValue.OfInt(enabled))
            is SORTERREF_SIZE -> arrayOf(VariadicValue.OfInt(nByte))

            is SQLLOG<*> -> arrayOf(
                sqllogFunctionPointer(callback as Sqlite3ConfigSqlLogCallback<Any?>?, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer),
                keyedStableRefPointer
                    ?.invoke(KEY_CONFIG_SQLLOG, callback, appData)
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
    option: Sqlite3VTabConfigOption,
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