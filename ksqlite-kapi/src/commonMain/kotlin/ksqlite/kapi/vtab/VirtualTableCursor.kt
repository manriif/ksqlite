package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.vtab.SqliteVTabCursor

/**
 * Represents a [VirtualTable] cursor used to read and/or write the virtual table.
 *
 * If an error is detected in a function exposed by this interface, and if error raising is allowed
 * by SQLite, it is allowed to raise an [SQLiteException] that is then reported to SQLite.
 */
public abstract class VirtualTableCursor : SqliteVTabCursor {

    /**
     * Returns `false` if `this` cursor currently points to a valid row of data, of `true`
     * otherwise.
     */
    public abstract fun eof(): Boolean

    /**
     * Begins a search on a virtual table.
     */
    public abstract fun VirtualTableFilterScope.filter(
        idxNum: Int,
        idxStr: String?,
        arguments: Array<ProtectedValue>
    )

    /**
     * Advances `this` cursor to the next row of a result set initiated by [filter].
     */
    public abstract fun next()

    /**
     * Returns the value of the [index]th column by using one of the
     * [VirtualTableColumnScope.setResult] overload.
     */
    public abstract fun VirtualTableColumnScope.column(index: Int)

    /**
     * Returns the rowid `this` cursor is currently pointing at.
     */
    public abstract fun rowid(): Long

    /**
     * Closes the cursor.
     * It is not allowed to throw an [SQLiteException] here.
     */
    public abstract fun close()
}