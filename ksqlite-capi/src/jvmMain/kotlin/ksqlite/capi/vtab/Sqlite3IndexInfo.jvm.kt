@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.vtab

import ksqlite.capi.convertVTabConstraintOperatorCode
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.sqlite3
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import ksqlite.sqlite3_index_info.sqlite3_index_constraint as s3_index_constraint
import ksqlite.sqlite3_index_info.sqlite3_index_constraint_usage as s3_index_constraint_usage
import ksqlite.sqlite3_index_info.sqlite3_index_orderby as s3_index_orderby

public actual class sqlite3_index_info internal constructor(pointer: MemorySegment) :
    Struct(Arena.ofConfined(), { s3_index_info.reinterpret(pointer, this, null) }) {

    private val constraints by lazy {
        val aConstraint = s3_index_info.aConstraint(this.pointer)
        Array(nConstraint) { s3_index_constraint.asSlice(aConstraint, it.toLong()) }
    }

    private val constraintUsages by lazy {
        val aConstraintUsage = s3_index_info.aConstraintUsage(this.pointer)
        Array(nConstraint) { s3_index_constraint_usage.asSlice(aConstraintUsage, it.toLong()) }
    }

    private val orderBys by lazy {
        val aOrderBy = s3_index_info.aOrderBy(this.pointer)
        Array(nConstraint) { s3_index_orderby.asSlice(aOrderBy, it.toLong()) }
    }

    public actual val colUsed: ULong
        get() = s3_index_info.colUsed(pointer).toULong()

    public actual val nConstraint: Int
        get() = s3_index_info.nConstraint(pointer)

    public actual val nOrderBy: Int
        get() = s3_index_info.nOrderBy(pointer)

    public actual fun getConstraintColumn(index: Int): Int =
        s3_index_constraint.iColumn(constraints[index])

    public actual fun getConstraintOp(index: Int): Sqlite3VTabConstraintOperatorCode =
        convertVTabConstraintOperatorCode(s3_index_constraint.op(constraints[index]).toInt())

    public actual fun getConstraintUsable(index: Int): Int =
        s3_index_constraint.usable(constraints[index]).toInt()

    public actual fun getOrderByColumn(index: Int): Int =
        s3_index_orderby.iColumn(orderBys[index])

    public actual fun getOrderByDesc(index: Int): Int =
        s3_index_orderby.desc(orderBys[index]).toInt()

    public actual var idxNum: Int
        get() = s3_index_info.idxNum(pointer)
        set(value) = s3_index_info.idxNum(pointer, value)

    public actual var idxStr: String?
        get() = s3_index_info.idxStr(pointer).toKStringFromUtf8OrNull()
        set(value) {
            sqlite3.sqlite3_free(s3_index_info.idxStr(pointer))
            s3_index_info.idxStr(pointer, sqlite3_mprintf(value))
        }

    public actual var needToFreeIdxStr: Int
        get() = s3_index_info.needToFreeIdxStr(pointer)
        set(value) = s3_index_info.needToFreeIdxStr(pointer, value)

    public actual var orderByConsumed: Int
        get() = s3_index_info.orderByConsumed(pointer)
        set(value) = s3_index_info.orderByConsumed(pointer, value)

    public actual var estimatedCost: Double
        get() = s3_index_info.estimatedCost(pointer)
        set(value) = s3_index_info.estimatedCost(pointer, value)

    public actual var estimatedRows: Long
        get() = s3_index_info.estimatedRows(pointer)
        set(value) = s3_index_info.estimatedRows(pointer, value)

    public actual var idxFlags: Sqlite3VTabScanFlag
        get() = Sqlite3VTabScanFlag.Mask(s3_index_info.idxFlags(pointer))
        set(value) = s3_index_info.idxFlags(pointer, value.value)

    public actual fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int) {
        s3_index_constraint_usage.argvIndex(constraintUsages[index], argvIndex)
    }

    public actual fun setConstraintUsageOmit(index: Int, omit: Int) {
        s3_index_constraint_usage.omit(constraintUsages[index], omit.toByte())
    }
}