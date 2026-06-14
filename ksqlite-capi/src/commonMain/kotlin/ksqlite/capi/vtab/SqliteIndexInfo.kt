@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.types.vtab.SqliteIndexInfo
import ksqlite.types.vtab.SqliteVTabConstraintOperatorCode
import ksqlite.types.vtab.SqliteVTabScanFlag

/**
 * The sqlite3_index_info structure and its substructures is used as part of the virtual table
 * interface to pass information into and receive the reply from the xBestIndex method of a virtual
 * table module.
 *
 * [sqlite3_index_info](https://sqlite.org/c3ref/index_info.html)
 */
public expect class sqlite3_index_info : Struct, SqliteIndexInfo {
    override val colUsed: ULong
    override val nConstraint: Int
    override val nOrderBy: Int
    override fun getConstraintColumn(index: Int): Int
    override fun getConstraintOp(index: Int): SqliteVTabConstraintOperatorCode
    override fun getConstraintUsable(index: Int): Int
    override fun getOrderByColumn(index: Int): Int
    override fun getOrderByDesc(index: Int): Int
    override var idxNum: Int
    override var idxStr: String?
    override var needToFreeIdxStr: Int
    override var orderByConsumed: Int
    override var estimatedCost: Double
    override var estimatedRows: Long
    override var idxFlags: SqliteVTabScanFlag
    override fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int)
    override fun setConstraintUsageOmit(index: Int, omit: Int)
}