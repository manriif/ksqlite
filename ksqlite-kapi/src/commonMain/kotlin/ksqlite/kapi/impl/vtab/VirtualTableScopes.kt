package ksqlite.kapi.impl.vtab

import ksqlite.capi.sqlite3_declare_vtab
import ksqlite.capi.sqlite3_overload_function
import ksqlite.capi.sqlite3_value_nochange
import ksqlite.capi.sqlite3_vtab_collation
import ksqlite.capi.sqlite3_vtab_config
import ksqlite.capi.sqlite3_vtab_distinct
import ksqlite.capi.sqlite3_vtab_in
import ksqlite.capi.sqlite3_vtab_in_first
import ksqlite.capi.sqlite3_vtab_in_next
import ksqlite.capi.sqlite3_vtab_nochange
import ksqlite.capi.sqlite3_vtab_on_conflict
import ksqlite.capi.sqlite3_vtab_rhs_value
import ksqlite.capi.types.Sqlite3ConflictResolutionMode
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3ValueOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.vtab.Sqlite3VTabConfigOption
import ksqlite.capi.types.vtab.Sqlite3VTabConstraintOperatorCode
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.kapi.impl.helpers.ClosableScope
import ksqlite.kapi.impl.helpers.ContextClosableScope
import ksqlite.kapi.impl.helpers.resultCheck
import ksqlite.kapi.impl.helpers.sqliteResultCheck
import ksqlite.kapi.impl.value.ValueReturnScopeImpl
import ksqlite.kapi.value.ProtectedValue
import ksqlite.kapi.value.ValueReturnScope
import ksqlite.kapi.value.toProtectedValue
import ksqlite.kapi.vtab.VirtualTableBestIndexScope
import ksqlite.kapi.vtab.VirtualTableColumnScope
import ksqlite.kapi.vtab.VirtualTableCreateOrConnectScope
import ksqlite.kapi.vtab.VirtualTableFilterScope
import ksqlite.kapi.vtab.VirtualTableFindFunctionScope
import ksqlite.kapi.vtab.VirtualTableIntegrityScope
import ksqlite.kapi.vtab.VirtualTableUpdateScope
import kotlin.concurrent.Volatile

internal class VirtualTableCreateOrConnectScope(private val db: sqlite3) :
    VirtualTableCreateOrConnectScope,
    ClosableScope() {

    override fun configure(options: List<Sqlite3VTabConfigOption>) = notClosed {
        options.forEach { option ->
            db.resultCheck(sqlite3_vtab_config(db, option))
        }
    }

    override fun declare(sql: String) =
        notClosed { db.resultCheck(sqlite3_declare_vtab(db, sql)) }

    override fun overloadFunction(name: String, argumentCount: Int) =
        notClosed { db.resultCheck(sqlite3_overload_function(db, name, argumentCount)) }
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
        val outValue = Sqlite3ValueOutputParam()
        val result = sqlite3_vtab_rhs_value(info, index, outValue)

        if (result != Sqlite3Result.NOTFOUND) {
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
     * the [ksqlite.kapi.vtab.VirtualTableCursor.filter] returns.
     */
    private inline fun createValue(
        block: (Sqlite3ValueOutputParam) -> Sqlite3Result
    ): ProtectedValue? = notClosed {
        lastValue?.scope?.close()

        val outValue = Sqlite3ValueOutputParam()
        val result = block(outValue)

        if (result is Sqlite3Result.DONE) {
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

    override fun close() {
        super.close()
        lastValue?.scope?.close()
    }
}

internal class VirtualTableFindFunctionScopeImpl :
    VirtualTableFindFunctionScope,
    ClosableScope() {

    var customCode: Sqlite3VTabConstraintOperatorCode.Custom? = null
        private set

    override fun customConstraintOperator(code: Sqlite3VTabConstraintOperatorCode.Custom) {
        customCode = code
    }
}

internal class VirtualTableUpdateScopeImpl(private val db: sqlite3) :
    VirtualTableUpdateScope,
    ClosableScope() {

    override val onConflict: Sqlite3ConflictResolutionMode
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