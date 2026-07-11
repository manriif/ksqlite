package ksqlite.kapi.vtab

import ksqlite.kapi.value.ValueReturnScope

/**
 * Scope to use with [VirtualTableCursor.column].
 */
public interface VirtualTableColumnScope : ValueReturnScope {

    /**
     * Returns `true` if and only if the call is during an UPDATE operation and the value of the
     * column will not be modified by the UPDATE.
     */
    public val nochange: Boolean
}