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
import ksqlite.capi.sqlite3_declare_vtab
import ksqlite.capi.sqlite3_overload_function
import ksqlite.capi.sqlite3_value
import ksqlite.capi.sqlite3_value_nochange
import ksqlite.capi.sqlite3_vtab_collation
import ksqlite.capi.sqlite3_vtab_distinct
import ksqlite.capi.sqlite3_vtab_in
import ksqlite.capi.sqlite3_vtab_in_first
import ksqlite.capi.sqlite3_vtab_in_next
import ksqlite.capi.sqlite3_vtab_nochange
import ksqlite.capi.sqlite3_vtab_on_conflict
import ksqlite.capi.sqlite3_vtab_rhs_value
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope
import ksqlite.kapi.helpers.ContextCloseableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.result.ResultScope
import ksqlite.kapi.result.ResultScopeImpl
import ksqlite.kapi.value.ProtectedValue
import ksqlite.kapi.value.toProtectedValue
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteResultCode
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import kotlin.concurrent.Volatile

internal class VirtualTableCreateOrConnectScopeImpl(private val db: sqlite3) :
    VirtualTableCreateOrConnectScope,
    UnsafeCloseableScope() {

    override val config = VirtualTableConfigurationImpl(db, this)

    override fun declare(sql: String) =
        notClosed { db.resultCheck(sqlite3_declare_vtab(db, sql)) }

    override fun overloadFunction(name: String, argumentCount: Int) =
        notClosed { sqliteResultCheck(sqlite3_overload_function(db, name, argumentCount)) }
}

internal class VirtualTableBestIndexScopeImpl(
    private val info: sqlite3_index_info,
) : VirtualTableBestIndexScope,
    UnsafeCloseableScope() {

    override val distinct: Int
        get() = notClosed { sqlite3_vtab_distinct(info) }

    override fun collation(index: Int): String =
        notClosed { sqlite3_vtab_collation(info, index) }

    override fun isIn(index: Int, handle: Int): Boolean =
        notClosed { sqlite3_vtab_in(info, index, handle) != 0 }

    override fun rhsValue(index: Int): ProtectedValue? = notClosed {
        val outValue = sqlite3_value.OutputParam()
        val result = sqlite3_vtab_rhs_value(info, index, outValue)

        if (result != SqliteResultCode.NOTFOUND) {
            sqliteResultCheck(result)
        }

        return outValue.value?.toProtectedValue(this)
    }
}

internal class VirtualTableColumnScopeImpl(private val scope: ContextCloseableScope) :
    VirtualTableColumnScope,
    ResultScope by ResultScopeImpl(scope) {

    override val nochange: Boolean
        get() = scope.notClosed { sqlite3_vtab_nochange(scope.context) != 0 }
}

internal class VirtualTableFilterScopeImpl :
    VirtualTableFilterScope,
    UnsafeCloseableScope() {

    @Volatile
    private var lastValue: ProtectedValue? = null

    /**
     * Returns a new [ProtectedValue] set from [block], invalidating any previously create value.
     *
     * According to SQLite, a value is invalidated by either call to [inFirst] or [inNext] or before
     * the [VirtualTableCursor.filter] returns.
     */
    private inline fun createValue(
        block: (sqlite3_value.OutputParam) -> SqliteResultCode
    ): ProtectedValue? = notClosed {
        lastValue?.scope?.close()

        val outValue = sqlite3_value.OutputParam()
        val result = block(outValue)

        if (result is SqliteResultCode.DONE) {
            return null
        }

        sqliteResultCheck(result)

        val valueScope = UnsafeCloseableScope()
        val value = outValue.value?.toProtectedValue(valueScope)
        lastValue = value

        return value
    }

    override fun inFirst(value: ProtectedValue): ProtectedValue? =
        createValue { sqlite3_vtab_in_first(value.value, it) }

    override fun inNext(value: ProtectedValue): ProtectedValue? =
        createValue { sqlite3_vtab_in_next(value.value, it) }

    override fun onClose() {
        lastValue?.scope?.close()
    }
}

internal class VirtualTableFindFunctionScopeImpl :
    VirtualTableFindFunctionScope,
    UnsafeCloseableScope() {

    var customCode: SqliteVtabConstraintOperatorCode.Custom? = null
        private set

    override fun customConstraintOperator(code: SqliteVtabConstraintOperatorCode.Custom) {
        customCode = code
    }
}

internal class VirtualTableUpdateScopeImpl(private val db: sqlite3) :
    VirtualTableUpdateScope,
    UnsafeCloseableScope() {

    override val onConflict: SqliteConflictResolutionMode
        get() = notClosed { sqlite3_vtab_on_conflict(db) }

    override val ProtectedValue.nochange: Boolean
        get() = notClosed { sqlite3_value_nochange(value) != 0 }
}

internal class VirtualTableIntegrityScopeImpl :
    VirtualTableIntegrityScope,
    UnsafeCloseableScope() {

    var message: String? = null
        private set

    override fun report(message: String) {
        this.message = message
    }
}