package ksqlite.capi

import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.OpaqueBuffer
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.memory.VariadicValue
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vtab.SqliteVtabConfigOption
import ksqlite.types.SqliteResultCode
import ksqlite.types.internal.convertResultCode

///////////////////////////////////////////////////////////////////////////
// Config
///////////////////////////////////////////////////////////////////////////

/**
 * Handles the [ksqlite.capi.sqlite3_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
@Suppress("UNCHECKED_CAST")
internal fun <Pointer : Any> commonConfig(
    option: SqliteConfigOption,
    bufferPointer: (OpaqueBuffer) -> Pointer?,
    logFunctionPointer: (callback: SqliteConfigLogCallback<Any?>?, appData: Any?) -> Pointer?,
    sqllogFunctionPointer: (callback: SqliteConfigSqlLogCallback<Any?>?, appData: Any?) -> Pointer?,
    keyedStableRefPointer: ((String, Any?, Any?) -> Pointer?)?,
    outputParamConfig: SqliteConfigOption.IntOutput.() -> Int,
    nativeConfig: (id: Int, args: Array<out VariadicValue<Pointer>?>) -> Int,
): SqliteResultCode {
    val args = with(option) {
        when (this) {
            is IntOutput -> return convertResultCode(outputParamConfig())
            SERIALIZED, MULTITHREAD, SINGLETHREAD -> emptyArray<VariadicValue<Pointer>>()
            is COVERING_INDEX_SCAN -> arrayOf(VariadicValue.OfInt(enabled))

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

            is PMASZ -> arrayOf(VariadicValue.OfUInt(szPma))
            is SMALL_MALLOC -> arrayOf(VariadicValue.OfInt(enabled))

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

    return convertResultCode(nativeConfig(option.id, args))
}

/**
 * Handles the [ksqlite.capi.sqlite3_db_config].
 * The array passed to [nativeConfig] contains at most 3 values.
 */
internal fun <Pointer : Any> commonDbConfig(
    option: SqliteDbConfigOption,
    bufferPointer: (OpaqueBuffer) -> Pointer?,
    outParamConfig: SqliteDbConfigOption.IntOutput.() -> Int,
    nativeConfig: (id: Int, values: Array<out VariadicValue<Pointer>?>) -> Int,
): SqliteResultCode {
    val args = with(option) {
        when (this) {
            is IntOutput -> {
                if (state != null) {
                    return convertResultCode(outParamConfig())
                }

                arrayOf(VariadicValue.OfInt(value), null)
            }

            is LOOKASIDE -> arrayOf(
                buf?.let(bufferPointer)?.let(VariadicValue<Pointer>::OfPointer),
                VariadicValue.OfInt(sz),
                VariadicValue.OfInt(cnt)
            )

            is MAINDBNAME -> arrayOf(VariadicValue.OfString(name, KEY_DB_CONFIG_MAINDBNAME))

            is RESET_DATABASE -> arrayOf(
                VariadicValue.OfInt(value),
                null
            )
        }
    }

    return convertResultCode(nativeConfig(option.id, args))
}

/**
 * Handles the [ksqlite.capi.sqlite3_vtab_config].
 * The array passed to [nativeConfig] contains at most 1 value.
 */
internal fun <Pointer : Any> commonVtabConfig(
    option: SqliteVtabConfigOption,
    nativeConfig: (id: Int, values: Array<out VariadicValue<Pointer>?>) -> Int,
): SqliteResultCode {
    val args = with(option) {
        when (this) {
            is CONSTRAINT_SUPPORT -> arrayOf(VariadicValue.OfInt(enabled))
            DIRECTONLY, INNOCUOUS, USES_ALL_SCHEMAS -> emptyArray()
        }
    }

    return convertResultCode(nativeConfig(option.id, args))
}

///////////////////////////////////////////////////////////////////////////
// Control
///////////////////////////////////////////////////////////////////////////

/**
 * Handles the [ksqlite.capi.sqlite3_file_control].
 */
internal fun commonFileControl(
    opcode: SqliteFileControlOpcode,
    control: () -> Int,
    controlBuffer: (Buffer?) -> Int,
    controlVfs: (sqlite3_vfs.OutputParam) -> Int,
    controlInt32: (Int32OutputParam) -> Int,
    controlInt64: (Int64OutputParam) -> Int,
    controlString: (param: Utf8OutputParam, freeOnRead: Boolean) -> Int
): SqliteResultCode = convertResultCode(
    when (opcode) {
        is IntParam -> controlInt32(opcode.param)
        is LongParam -> controlInt64(opcode.param)
        is Custom -> controlBuffer(opcode.buffer)
        is RESET_CACHE -> control()
        is TEMPFILENAME, is VFSNAME -> controlString(opcode.param, true)
        is VFS_POINTER -> controlVfs(opcode.param)
    }
)