package ksqlite.kapi.vtab

import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.vtab.SqliteIndexInfo

/**
 * Scope to use with [VirtualTable.bestIndex].
 */
public interface VirtualTableBestIndexScope {

    /**
     * Returns an integer value between 0 and 3 that help determining if the query is distinct.
     */
    public val distinct: Int

    /**
     * Returns the name of the collation sequence to use for text comparisons on the constraint
     * the received [SqliteIndexInfo] and constraint at [index].
     */
    public fun collation(index: Int): String

    /**
     * Returns `true` if the [index]th constraint is a IN() constraint, or `false` otherwise.
     */
    public fun isIn(
        index: Int,
        handle: Int
    ): Boolean

    /**
     * Returns the right-hand-side value for the [index]th constraint.
     */
    public fun rhsValue(index: Int): ProtectedValue?
}