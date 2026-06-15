package ksqlite.capi

import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.VariadicValue
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.types.SqliteResultCode
import ksqlite.types.internal.convertResult
import ksqlite.types.vtab.SqliteVTabConfigOption

/**
 * Handles the [ksqlite.capi.sqlite3_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
@Suppress("UNCHECKED_CAST")
internal fun <Pointer : Any> commonConfig(
    option: SqliteConfigOption,
    bufferPointer: (Buffer) -> Pointer?,
    logFunctionPointer: (callback: SqliteConfigLogCallback<Any?>?, appData: Any?) -> Pointer?,
    sqllogFunctionPointer: (callback: SqliteConfigSqlLogCallback<Any?>?, appData: Any?) -> Pointer?,
    keyedStableRefPointer: ((String, Any?, Any?) -> Pointer?)?,
    rowidInView: SqliteConfigOption.ROWID_IN_VIEW.() -> Int,
    nativeConfig: (id: Int, args: Array<out VariadicValue<Pointer>?>) -> Int,
): SqliteResultCode {
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
                logFunctionPointer(callback as SqliteConfigLogCallback<Any?>?, appData)
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
                sqllogFunctionPointer(callback as SqliteConfigSqlLogCallback<Any?>?, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer),
                keyedStableRefPointer
                    ?.invoke(KEY_CONFIG_SQLLOG, callback, appData)
                    ?.let(VariadicValue<Pointer>::OfPointer)
            )

            is STMTJRNL_SPILL -> arrayOf(VariadicValue.OfInt(nByte))
            is URI -> arrayOf(VariadicValue.OfInt(value))
        }
    }

    return convertResult(nativeConfig(option.id, args))
}

/**
 * Handles the [ksqlite.capi.sqlite3_db_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
internal fun <Pointer : Any> commonDbConfig(
    option: SqliteDbConfigOption,
    bufferPointer: (Buffer) -> Pointer?,
    outParamConfig: SqliteDbConfigOption.IntOutput.() -> Int,
    nativeConfig: (id: Int, values: Array<out VariadicValue<Pointer>?>) -> Int,
): SqliteResultCode {
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
    option: SqliteVTabConfigOption,
    nativeConfig: (id: Int, values: Array<out VariadicValue<Pointer>?>) -> Int,
): SqliteResultCode {
    val args = with(option) {
        when (this) {
            is CONSTRAINT_SUPPORT -> arrayOf(VariadicValue.OfInt(enabled))
            DIRECTONLY, INNOCUOUS, USES_ALL_SCHEMAS -> emptyArray()
        }
    }

    return convertResult(nativeConfig(option.id, args))
}