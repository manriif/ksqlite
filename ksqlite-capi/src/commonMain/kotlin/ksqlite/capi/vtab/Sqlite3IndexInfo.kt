package ksqlite.capi.vtab

/**
 * Definition of [sqlite3_index_info].
 */
public interface Sqlite3IndexInfo {

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
    public fun getConstraintOp(index: Int): Sqlite3VTabConstraintOperatorCode

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
     * Mask of [Sqlite3VTabScanFlag] flags.
     */
    public var idxFlags: Sqlite3VTabScanFlag

    /**
     * if [argvIndex] > 0, then the constraint at [index] is part of argv to xFilter.
     */
    public fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int)

    /**
     * If [omit] is `True` then no test is coded for the constraint at [index].
     */
    public fun setConstraintUsageOmit(index: Int, omit: Int)
}