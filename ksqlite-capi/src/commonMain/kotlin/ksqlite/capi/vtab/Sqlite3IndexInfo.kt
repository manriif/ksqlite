package ksqlite.capi.vtab

/**
 * Definition of [sqlite3_index_info].
 */
public interface Sqlite3IndexInfo {

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
    public fun constraintColumn(index: Int): Int

    /**
     * Returns the operator for the constraint at [index].
     */
    public fun constraintOperator(index: Int): Sqlite3VTabConstraintOperatorCode

    /**
     * Returns true if the constraint at [index] is usable.
     */
    public fun constraintUsable(index: Int): Boolean

    /**
     * Returns the column for the ORDER BY at [index].
     */
    public fun orderByColumn(index: Int): Int

    /**
     * Returns `true` in the ORDER BY at [index] is in descending order, `false` if the order is
     * ascending.
     */
    public fun orderByDesc(index: Int): Boolean
}