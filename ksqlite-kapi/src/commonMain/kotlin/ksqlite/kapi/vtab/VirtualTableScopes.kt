package ksqlite.kapi.vtab

import ksqlite.capi.sqlite3_declare_vtab
import ksqlite.capi.sqlite3_overload_function
import ksqlite.capi.sqlite3_value_nochange
import ksqlite.capi.sqlite3_vtab_collation
import ksqlite.capi.sqlite3_vtab_distinct
import ksqlite.capi.sqlite3_vtab_in
import ksqlite.capi.sqlite3_vtab_in_first
import ksqlite.capi.sqlite3_vtab_in_next
import ksqlite.capi.sqlite3_vtab_nochange
import ksqlite.capi.sqlite3_vtab_on_conflict
import ksqlite.capi.sqlite3_vtab_rhs_value
import ksqlite.capi.types.SqliteValueOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.ContextClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.value.ProtectedValue
import ksqlite.kapi.value.ValueReturnScope
import ksqlite.kapi.value.ValueReturnScopeImpl
import ksqlite.kapi.value.toProtectedValue
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteResultCode
import ksqlite.types.vtab.SqliteVTabConstraintOperatorCode
import kotlin.concurrent.Volatile

internal class VirtualTableCreateOrConnectScopeImpl(private val db: sqlite3) :
    VirtualTableCreateOrConnectScope,
    ClosableScope() {

    override fun configure(action: VirtualTableConfigurationScope.() -> Unit) =
        notClosed { VirtualTableConfigurationScopeImpl(db).use(action) }

    override fun declare(sql: String) =
        notClosed { sqliteResultCheck(sqlite3_declare_vtab(db, sql)) }

    override fun overloadFunction(name: String, argumentCount: Int) =
        notClosed { sqliteResultCheck(sqlite3_overload_function(db, name, argumentCount)) }
}

internal class VirtualTableBestIndexScopeImpl(
    private val info: sqlite3_index_info,
) : VirtualTableBestIndexScope,
    ClosableScope() {

    override val distinct: Int
        get() = notClosed { sqlite3_vtab_distinct(info) }

    override fun collation(index: Int): String =
        notClosed { sqlite3_vtab_collation(info, index) }

    override fun isIn(index: Int, handle: Int): Boolean =
        notClosed { sqlite3_vtab_in(info, index, handle) != 0 }

    override fun rhsValue(index: Int): ProtectedValue? = notClosed {
        val outValue = SqliteValueOutputParam()
        val result = sqlite3_vtab_rhs_value(info, index, outValue)

        if (result != SqliteResultCode.NOTFOUND) {
            sqliteResultCheck(result)
        }

        return outValue.value?.toProtectedValue(this)
    }
}

internal class VirtualTableColumnScopeImpl(private val scope: ContextClosableScope) :
    VirtualTableColumnScope,
    ValueReturnScope by ValueReturnScopeImpl(scope) {

    override val nochange: Boolean
        get() = scope.notClosed { sqlite3_vtab_nochange(scope.context) != 0 }
}

internal class VirtualTableFilterScopeImpl :
    VirtualTableFilterScope,
    ClosableScope() {

    @Volatile
    private var lastValue: ProtectedValue? = null

    /**
     * Returns a new [ProtectedValue] set from [block], invalidating any previously create value.
     *
     * According to SQLite, a value is invalidated by either call to [inFirst] or [inNext] or before
     * the [VirtualTableCursor.filter] returns.
     */
    private inline fun createValue(
        block: (SqliteValueOutputParam) -> SqliteResultCode
    ): ProtectedValue? = notClosed {
        lastValue?.scope?.close()

        val outValue = SqliteValueOutputParam()
        val result = block(outValue)

        if (result is SqliteResultCode.DONE) {
            return null
        }

        sqliteResultCheck(result)

        val valueScope = ClosableScope()
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
    ClosableScope() {

    var customCode: SqliteVTabConstraintOperatorCode.Custom? = null
        private set

    override fun customConstraintOperator(code: SqliteVTabConstraintOperatorCode.Custom) {
        customCode = code
    }
}

internal class VirtualTableUpdateScopeImpl(private val db: sqlite3) :
    VirtualTableUpdateScope,
    ClosableScope() {

    override val onConflict: SqliteConflictResolutionMode
        get() = notClosed { sqlite3_vtab_on_conflict(db) }

    override val ProtectedValue.nochange: Boolean
        get() = notClosed { sqlite3_value_nochange(value) != 0 }
}

internal class VirtualTableIntegrityScopeImpl :
    VirtualTableIntegrityScope,
    ClosableScope() {

    var message: String? = null
        private set

    override fun report(message: String) {
        this.message = message
    }
}