@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.convertVTabConstraintOperatorCode
import ksqlite.capi.memory.Struct
import ksqlite.sqlite3_free
import ksqlite.sqlite3_mprintf

public actual class sqlite3_index_info
internal constructor(override val pointer: CPointer<s3_index_info>) : Struct(pointer) {

    private inline val info: s3_index_info
        get() = pointer.pointed

    public actual val colUsed: ULong
        get() = info.colUsed

    public actual val nConstraint: Int
        get() = info.nConstraint

    public actual val nOrderBy: Int
        get() = info.nOrderBy

    public actual fun getConstraintColumn(index: Int): Int =
        info.aConstraint!![index].iColumn

    public actual fun getConstraintOp(index: Int): Sqlite3VTabConstraintOperatorCode =
        convertVTabConstraintOperatorCode(info.aConstraint!![index].op.toInt())

    public actual fun getConstraintUsable(index: Int): Int =
        info.aConstraint!![index].usable.toInt()

    public actual fun getOrderByColumn(index: Int): Int =
        info.aOrderBy!![index].iColumn

    public actual fun getOrderByDesc(index: Int): Int =
        info.aOrderBy!![index].desc.toInt()

    public actual var idxNum: Int
        get() = info.idxNum
        set(value) {
            info.idxNum = value
        }

    public actual var idxStr: String?
        get() = info.idxStr?.toKStringFromUtf8()
        set(value) {
            sqlite3_free(info.idxStr)
            info.idxStr = value?.let(::sqlite3_mprintf)
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
        info.aConstraintUsage!![index].argvIndex = argvIndex
    }

    public actual fun setConstraintUsageOmit(index: Int, omit: Int) {
        info.aConstraintUsage!![index].omit = omit.convert()
    }
}