@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.convertVTabConstraintOperatorCode
import ksqlite.capi.exports
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.structs.invoke
import ksqlite.structs.nthConstraint
import ksqlite.structs.nthConstraintUsage
import ksqlite.structs.nthOrderBy
import ksqlite.wasm.WasmPointer
import kotlin.js.toJsBigInt
import kotlin.js.toLong

public actual class sqlite3_index_info private constructor(private val info: s3_index_info) :
    Struct(info.pointer) {

    internal constructor(pointer: WasmPointer) : this(capi.sqlite3_index_info(pointer))

    // Lists are used instead of arrays because Array is broken in webMain sourceSet
    private val constraints by lazy { List(nConstraint, info::nthConstraint) }
    private val constraintUsages by lazy { List(nConstraint, info::nthConstraintUsage) }
    private val orderBys by lazy { List(nConstraint, info::nthOrderBy) }

    public actual val colUsed: ULong
        get() = info.colUsed.toLong().toULong()

    public actual val nConstraint: Int
        get() = info.nConstraint

    public actual val nOrderBy: Int
        get() = info.nOrderBy

    public actual fun getConstraintColumn(index: Int): Int =
        constraints[index].iColumn

    public actual fun getConstraintOp(index: Int): Sqlite3VTabConstraintOperatorCode =
        convertVTabConstraintOperatorCode(constraints[index].op)

    public actual fun getConstraintUsable(index: Int): Int =
        constraints[index].usable

    public actual fun getOrderByColumn(index: Int): Int =
        orderBys[index].iColumn

    public actual fun getOrderByDesc(index: Int): Int =
        orderBys[index].desc

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
        constraintUsages[index].argvIndex = argvIndex
    }

    public actual fun setConstraintUsageOmit(index: Int, omit: Int) {
        constraintUsages[index].omit = omit
    }
}