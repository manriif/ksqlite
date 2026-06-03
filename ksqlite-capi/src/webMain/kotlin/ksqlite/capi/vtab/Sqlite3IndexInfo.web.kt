@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.vtab

import ksqlite.capi.exports
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.wasm.WasmPointer
import kotlin.js.toJsBigInt
import kotlin.js.toLong

public actual class sqlite3_index_info internal constructor(private val info: s3_index_info) :
    Struct(info.pointer) {

    internal constructor(pointer: WasmPointer) : this(s3_index_info(pointer))

    public actual val colUsed: ULong
        get() = info.colUsed.toLong().toULong()

    public actual val nConstraint: Int
        get() = info.nConstraint

    public actual val nOrderBy: Int
        get() = info.nOrderBy

    public actual fun getConstraintColumn(index: Int): Int =
        TODO()

    public actual fun getConstraintOp(index: Int): Sqlite3VTabConstraintOperatorCode =
        TODO()// convertVTabConstraintOperatorCode()

    public actual fun getConstraintUsable(index: Int): Int =
        TODO()

    public actual fun getOrderByColumn(index: Int): Int =
        TODO()

    public actual fun getOrderByDesc(index: Int): Int =
        TODO()

    public actual var idxNum: Int
        get() = info.idxNum
        set(value) {
            info.idxNum = value
        }

    public actual var idxStr: String?
        get() = info.idxStr.toKStringFromUtf8OrNull()
        set(value) {
            exports.sqlite3_free(info.idxStr)
            info.idxStr = value?.let(::sqlite3_mprintf).notNull
        }

    public actual var needToFreeIdxStr: Int
        get() = info.needToFreeIdxStr
        set(value) {
            info.needToFreeIdxStr = value
        }

    public actual var orderByConsumed: Int
        get() = info.orderByConsumed
        set(value) {
            info.orderByConsumed = value
        }

    public actual var estimatedCost: Double
        get() = info.estimatedCost
        set(value) {
            info.estimatedCost = value
        }

    public actual var estimatedRows: Long
        get() = info.estimatedRows.toLong()
        set(value) {
            info.estimatedRows = value.toJsBigInt()
        }

    public actual var idxFlags: Sqlite3VTabScanFlag
        get() = Sqlite3VTabScanFlag.Mask(info.idxFlags)
        set(value) {
            info.idxFlags = value.value
        }

    public actual fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int) {
        TODO()
    }

    public actual fun setConstraintUsageOmit(index: Int, omit: Int) {
        TODO()
    }
}