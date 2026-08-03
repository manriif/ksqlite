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

import ksqlite.capi.sqlite3
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.helpers.runCatchingSQLiteException
import ksqlite.types.SqliteResultCode

/**
 * Implementation of [sqlite3_vtab].
 */
internal class Vtab(
    internal val table: VirtualTable,
    internal val db: sqlite3
) : sqlite3_vtab() {

    /**
     * Sets the [errMsg] from [exception]'s message and returns [exception]'s result
     */
    fun handleError(exception: SQLiteException): SqliteResultCode.Failure {
        errMsg = exception.message
        return exception.result
    }

    /**
     * Executes [block] and returns [SqliteResultCode.OK] or an instance of [SqliteResultCode.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: VirtualTable.() -> Unit): SqliteResultCode.OkOrFailure {
        return table.runCatchingSQLiteException(::handleError) {
            block()
            SqliteResultCode.OK
        }
    }

    /**
     * Executes [block] and returns its result. If an error is thrown, it is passed to [transform]
     * and the result is returned.
     */
    inline fun <T> catching(
        transform: (SqliteResultCode.Failure) -> T,
        block: VirtualTable.() -> T
    ): T = table.runCatchingSQLiteException({ transform(handleError(it)) }) { block() }
}

/**
 * Implementation of [sqlite3_vtab_cursor].
 */
internal class VtabCursor(
    internal val cursor: VirtualTableCursor,
    internal val vTab: Vtab,
) : sqlite3_vtab_cursor() {

    /**
     * Executes [block] and returns [SqliteResultCode.OK] or an instance of [SqliteResultCode.Failure] if
     * [block]'s throws.
     */
    inline fun catching(block: VirtualTableCursor.() -> Unit): SqliteResultCode.OkOrFailure {
        return cursor.runCatchingSQLiteException(vTab::handleError) {
            block()
            SqliteResultCode.OK
        }
    }

    /**
     * Executes [block] and returns its result. If an error is thrown, it is passed to [transform]
     * and the result is returned.
     */
    inline fun <T> catching(
        transform: (SqliteResultCode.Failure) -> T,
        block: VirtualTableCursor.() -> T
    ): T = cursor.runCatchingSQLiteException({ transform(vTab.handleError(it)) }) { block() }
}