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
package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.result.Result
import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.vtab.SqliteVtabCursor

/**
 * Represents a [VirtualTable] cursor used to read and/or write the virtual table.
 *
 * If an error is detected in a function exposed by this interface, and if error raising is allowed
 * by SQLite, it is allowed to raise an [SQLiteException] that is then reported to SQLite.
 */
public abstract class VirtualTableCursor : SqliteVtabCursor {

    /**
     * Returns `false` if this cursor currently points to a valid row of data, `true` otherwise.
     */
    public abstract fun eof(): Boolean

    /**
     * Begins a search of the virtual table using the query plan selected by
     * [VirtualTable.bestIndex]. [idxNum] and [idxStr] carry whatever values
     * [VirtualTable.bestIndex] set on the index info, and [arguments] holds the value of each
     * constraint marked as usable in that plan, in the order they were requested.
     */
    public abstract fun VirtualTableFilterScope.filter(
        idxNum: Int,
        idxStr: String?,
        arguments: Array<ProtectedValue>
    )

    /**
     * Advances this cursor to the next row of the result set initiated by [filter].
     */
    public abstract fun next()

    /**
     * Returns the value of the [index]th column. The returned [Result] must come from
     * calling one of the receiver [VirtualTableColumnScope]'s result methods.
     */
    public abstract fun VirtualTableColumnScope.column(index: Int): Result

    /**
     * Returns the rowid this cursor is currently pointing at.
     */
    public abstract fun rowid(): Long

    /**
     * Closes the cursor.
     * It is not allowed to throw an [SQLiteException] here.
     */
    public abstract fun close()
}