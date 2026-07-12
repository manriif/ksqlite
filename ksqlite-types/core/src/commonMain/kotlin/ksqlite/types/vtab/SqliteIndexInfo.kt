/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.types.vtab

/**
 * Describes an [`sqlite3_index_info`](https://sqlite.org/c3ref/index_info.html) struct.
 */
public interface SqliteIndexInfo {

    ///////////////////////////////////////////////////////////////////////////
    // Inputs
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Mask of columns used by statement.
     */
    public val colUsed: ULong

    /**
     * Number of constraints.
     */
    public val nConstraint: Int

    /**
     * Number of terms in the ORDER BY clause.
     */
    public val nOrderBy: Int

    /**
     * Returns the column for the constraint at [index].
     * Returns -1 for ROWID.
     */
    public fun getConstraintColumn(index: Int): Int

    /**
     * Returns the operator for the constraint at [index].
     */
    public fun getConstraintOp(index: Int): SqliteVtabConstraintOperatorCode

    /**
     * Returns `True` if the constraint at [index] is usable.
     */
    public fun getConstraintUsable(index: Int): Int

    /**
     * Returns the column for the ORDER BY at [index].
     */
    public fun getOrderByColumn(index: Int): Int

    /**
     * Returns `True` in the ORDER BY at [index] is in descending order, `False` if the order is
     * ascending.
     */
    public fun getOrderByDesc(index: Int): Int

    ///////////////////////////////////////////////////////////////////////////
    // Outputs
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Number used to identify the index.
     */
    public var idxNum: Int

    /**
     * String recorded with [idxNum] and passed into the xFilter method.
     * Setting a value to this field automatically free any previously existing value.
     */
    public var idxStr: String?

    /**
     * Free [idxStr] using sqlite3_free() if true.
     */
    public var needToFreeIdxStr: Int

    /**
     * True if output is already ordered.
     */
    public var orderByConsumed: Int

    /**
     * Estimated cost of using this index.
     */
    public var estimatedCost: Double

    /**
     * Estimated number of rows returned.
     */
    public var estimatedRows: Long

    /**
     * Mask of [ksqlite.types.vtab.SqliteVtabScanFlag] flags.
     */
    public var idxFlags: SqliteVtabScanFlag

    /**
     * if [argvIndex] > 0, then the constraint at [index] is part of argv to xFilter.
     */
    public fun setConstraintUsageArgvIndex(
        index: Int,
        argvIndex: Int
    )

    /**
     * If [omit] is `True` then no test is coded for the constraint at [index].
     */
    public fun setConstraintUsageOmit(
        index: Int,
        omit: Int
    )
}