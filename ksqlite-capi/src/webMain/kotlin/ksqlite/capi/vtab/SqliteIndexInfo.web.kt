@file:Suppress("ClassName", "SpellCheckingInspection", "REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.exports
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.foreign.structs.invoke
import ksqlite.foreign.structs.nthConstraint
import ksqlite.foreign.structs.nthConstraintUsage
import ksqlite.foreign.structs.nthOrderBy
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.types.internal.convertVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteIndexInfo
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteVtabScanFlag
import kotlin.js.toJsBigInt
import kotlin.js.toLong

public actual class sqlite3_index_info private constructor(private val info: s3_index_info) :
    Struct(info.pointer),
    SqliteIndexInfo {

    internal constructor(pointer: WasmPointer) : this(capi.sqlite3_index_info(pointer))

    // Lists are used instead of arrays because Array is broken in webMain sourceSet
    private val constraints by lazy { List(nConstraint, info::nthConstraint) }
    private val constraintUsages by lazy { List(nConstraint, info::nthConstraintUsage) }
    private val orderBys by lazy { List(nConstraint, info::nthOrderBy) }

    public actual override val colUsed: ULong
        get() = info.colUsed.toLong().toULong()

    public actual override val nConstraint: Int
        get() = info.nConstraint

    public actual override val nOrderBy: Int
        get() = info.nOrderBy

    public actual override fun getConstraintColumn(index: Int): Int =
        constraints[index].iColumn

    public actual override fun getConstraintOp(index: Int): SqliteVtabConstraintOperatorCode =
        convertVtabConstraintOperatorCode(constraints[index].op)

    public actual override fun getConstraintUsable(index: Int): Int =
        constraints[index].usable

    public actual override fun getOrderByColumn(index: Int): Int =
        orderBys[index].iColumn

    public actual override fun getOrderByDesc(index: Int): Int =
        orderBys[index].desc

    public actual override var idxNum: Int
        get() = info.idxNum
        set(value) {
            info.idxNum = value
        }

    public actual override var idxStr: String?
        get() = info.idxStr.toKStringFromUtf8OrNull()
        set(value) {
            exports.sqlite3_free(info.idxStr)
            info.idxStr = value?.let(::sqlite3_mprintf).notNull
        }

    public actual override var needToFreeIdxStr: Int
        get() = info.needToFreeIdxStr
        set(value) {
            info.needToFreeIdxStr = value
        }

    public actual override var orderByConsumed: Int
        get() = info.orderByConsumed
        set(value) {
            info.orderByConsumed = value
        }

    public actual override var estimatedCost: Double
        get() = info.estimatedCost
        set(value) {
            info.estimatedCost = value
        }

    public actual override var estimatedRows: Long
        get() = info.estimatedRows.toLong()
        set(value) {
            info.estimatedRows = value.toJsBigInt()
        }

    public actual override var idxFlags: SqliteVtabScanFlag
        get() = SqliteVtabScanFlag.from(info.idxFlags)
        set(value) {
            info.idxFlags = value.value
        }

    public actual override fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int) {
        constraintUsages[index].argvIndex = argvIndex
    }

    public actual override fun setConstraintUsageOmit(index: Int, omit: Int) {
        constraintUsages[index].omit = omit
    }
}