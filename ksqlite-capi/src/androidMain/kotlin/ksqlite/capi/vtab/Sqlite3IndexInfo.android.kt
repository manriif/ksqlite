@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.vtab

import ksqlite.capi.convertVTabConstraintOperatorCode
import ksqlite.capi.memory.JniPointer
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf

public actual class sqlite3_index_info private constructor(private val info: s3_index_info) :
    Struct(info.pointer, null /* memory is owned by SQLite */) {

    internal constructor(pointer: JniPointer) : this(s3_index_info(pointer))

    private val constraints by lazy { Array(nConstraint, info::constraint) }
    private val constraintUsages by lazy { Array(nConstraint, info::constraintUsage) }
    private val orderBys by lazy { Array(nConstraint, info::orderBy) }

    public actual val colUsed: ULong
        get() = info.colUsed.toULong()

    public actual val nConstraint: Int
        get() = info.nConstraint

    public actual val nOrderBy: Int
        get() = info.nOrderBy

    public actual fun getConstraintColumn(index: Int): Int =
        constraints[index].iColumn

    public actual fun getConstraintOp(index: Int): Sqlite3VTabConstraintOperatorCode =
        convertVTabConstraintOperatorCode(constraints[index].op.toInt())

    public actual fun getConstraintUsable(index: Int): Int =
        constraints[index].usable.toInt()

    public actual fun getOrderByColumn(index: Int): Int =
        orderBys[index].iColumn

    public actual fun getOrderByDesc(index: Int): Int =
        orderBys[index].desc.toInt()

    public actual var idxNum: Int
        get() = info.idxNum
        set(value) {
            info.idxNum = value
        }

    public actual var idxStr: String?
        get() = info.idxStr.toKStringFromUtf8OrNull()
        set(value) = sqlite3_mprintf(info::idxStr, value)

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
        get() = info.estimatedRows
        set(value) {
            info.estimatedRows = value
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
        constraintUsages[index].omit = omit.toByte()
    }
}