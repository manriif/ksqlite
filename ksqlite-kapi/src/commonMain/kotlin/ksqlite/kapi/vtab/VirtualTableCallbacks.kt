package ksqlite.kapi.vtab

import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.sqlite3_result_text
import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.callbacks.SqliteVTabBestIndexCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabCloseCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabColumnCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabCreateOrConnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabDestroyOrDisconnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabEofCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabFilterCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabIntegrityCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabNestedTransactionCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabNextCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabOpenCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabRenameCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabRowidCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabTransactionCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabUpdateCallback
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.functions.ScalarFunctionFuncCallback
import ksqlite.kapi.helpers.AutoCloser
import ksqlite.kapi.helpers.ContextClosableScope
import ksqlite.kapi.helpers.runCatchingSQLiteException
import ksqlite.kapi.sqliteRequireConnection
import ksqlite.kapi.value.toProtectedValues

/**
 * Invokes [VirtualTableModule.close] and closes the [VirtualTableModule.module].
 */
internal val VirtualTableModuleDestructor = SqliteDestroyCallback { module: VirtualTableModule ->
    module.moduleLock.withLock {
        checkNotNull(module.module).close()
        module.module = null
    }

    module.close()
}

/**
 * Common logic for [VTabCreateCallback] and [VTabConnectCallback].
 */
private inline fun <Module : VirtualTableModule> createOrConnect(
    crossinline block: Module.(
        scope: VirtualTableCreateOrConnectScope,
        connection: DatabaseConnection,
        arguments: Array<String>
    ) -> VirtualTable
) = SqliteVTabCreateOrConnectCallback<Module, VTab> { db, module, arguments ->
    module.runCatchingSQLiteException({ failure(it.message) }) {
        val connection = sqliteRequireConnection(db)

        val table = VirtualTableCreateOrConnectScopeImpl(db).use { scope ->
            block(module, scope, connection, arguments)
        }

        val vTab = VTab(table, db)
        table.parent = vTab

        success(vTab)
    }
}

/**
 * Invokes [VirtualTableModule.Regular.create]
 */
internal val VTabCreateCallback =
    createOrConnect<VirtualTableModule.Regular> { scope, connection, arguments ->
        scope.create(connection, arguments)
    }

/**
 * Invokes [VirtualTableModule.connect]
 */
internal val VTabConnectCallback =
    createOrConnect<VirtualTableModule> { scope, connection, arguments ->
        scope.connect(connection, arguments)
    }

/**
 * Invokes [VirtualTable.bestIndex].
 */
internal val VTabBestIndexCallback = SqliteVTabBestIndexCallback { vTab: VTab, info ->
    vTab.catching {
        VirtualTableBestIndexScopeImpl(info).use { it.bestIndex(info) }
    }
}

/**
 * Common logic for [VTabCloseCallback] and [VTabDisconnectCallback].
 */
private inline fun disconnectOrDestroy(crossinline block: VirtualTable.() -> Unit) =
    SqliteVTabDestroyOrDisconnectCallback { vTab: VTab ->
        try {
            vTab.catching(block)
        } finally {
            vTab.table.parent = null
        }
    }

/**
 * Invokes [VirtualTable.disconnect].
 */
internal val VTabDisconnectCallback = disconnectOrDestroy(VirtualTable::disconnect)

/**
 * Invokes [VirtualTable.destroy].
 */
internal val VTabDestroyCallback = disconnectOrDestroy(VirtualTable::destroy)

/**
 * Invokes [VirtualTable.open].
 */
internal val VTabOpenCallback = SqliteVTabOpenCallback { vTab: VTab ->
    vTab.catching(::failure) {
        val cursor = open()
        val vTabCursor = VTabCursor(cursor, vTab)
        success(vTabCursor)
    }
}

/**
 * Invokes [VirtualTableCursor.close].
 */
internal val VTabCloseCallback = SqliteVTabCloseCallback { cursor: VTabCursor ->
    try {
        cursor.catching(VirtualTableCursor::close)
    } finally {
        cursor.cursor.close()
    }
}

/**
 * Invokes [VirtualTableCursor.eof].
 */
internal val VTabEofCallback = SqliteVTabEofCallback { cursor: VTabCursor ->
    cursor.catching({ -1 }) { if (eof()) 1 else 0 }
}

/**
 * Invokes [VirtualTableCursor.filter].
 */
internal val VTabFilterCallback =
    SqliteVTabFilterCallback { cursor: VTabCursor, idxNum, idxStr, arguments ->
        cursor.catching {
            VirtualTableFilterScopeImpl().use { scope ->
                scope.filter(idxNum, idxStr, arguments.toProtectedValues(scope))
            }
        }
    }

/**
 * Invokes [VirtualTableCursor.next].
 */
internal val VTabNextCallback = SqliteVTabNextCallback { cursor: VTabCursor ->
    cursor.catching(VirtualTableCursor::next)
}

/**
 * Invokes [VirtualTableCursor.column].
 */
internal val VTabColumnCallback = SqliteVTabColumnCallback { cursor: VTabCursor, context, index ->
    cursor.cursor.runCatchingSQLiteException({ error ->
        // sqlite3_result_text() must be used here to set the error message according to SQLite
        sqlite3_result_text(context, error.message)
        error.result
    }) {
        ContextClosableScope(context).use { scope ->
            VirtualTableColumnScopeImpl(scope).column(index)
        }

        SqliteResultCode.OK
    }
}

/**
 * Invokes [VirtualTableCursor.rowid].
 */
internal val VTabRowidCallback = SqliteVTabRowidCallback { cursor: VTabCursor ->
    cursor.catching(::failure) { success(rowid()) }
}

/**
 * Invokes [VirtualTable.update].
 */
internal val VTabUpdateCallback = SqliteVTabUpdateCallback { vTab: VTab, arguments ->
    vTab.catching(::failure) {
        VirtualTableUpdateScopeImpl(vTab.db).use { scope ->
            success(scope.update(arguments.toProtectedValues(scope)))
        }
    }
}

/**
 * Invokes [VirtualTable.findFunction].
 */
internal val VTabFindFunctionCallback = SqliteVTabFindFunctionCallback { vTab: VTab, argc, name ->
    vTab.catching({ doNotOverload() }) {
        VirtualTableFindFunctionScopeImpl().use { scope ->
            scope.findFunction(name, argc)?.let { function ->
                scope.customCode
                    ?.let { overload(it, function, ScalarFunctionFuncCallback, AutoCloser) }
                    ?: overload(function, ScalarFunctionFuncCallback, AutoCloser)
            } ?: scope.customCode?.let { code ->
                error(
                    "Custom constraint operator code can only be set if a function is returned " +
                            "but $code was passed"
                )
            } ?: doNotOverload()
        }
    }
}

/**
 * Common logic for [VTabBeginCallback], [VTabSyncCallback], [VTabCommitCallback] and
 * [VTabRollbackCallback].
 */
private inline fun transaction(crossinline block: VirtualTable.() -> Unit) =
    SqliteVTabTransactionCallback { vTab: VTab ->
        vTab.catching(block)
    }

/**
 * Invokes [VirtualTable.begin].
 */
internal val VTabBeginCallback = transaction(VirtualTable::begin)

/**
 * Invokes [VirtualTable.sync].
 */
internal val VTabSyncCallback = transaction(VirtualTable::sync)

/**
 * Invokes [VirtualTable.commit].
 */
internal val VTabCommitCallback = transaction(VirtualTable::commit)

/**
 * Invokes [VirtualTable.rollback].
 */
internal val VTabRollbackCallback = transaction(VirtualTable::rollback)

/**
 * Invokes [VirtualTable.rename].
 */
internal val VTabRenameCallback = SqliteVTabRenameCallback { vTab: VTab, newName ->
    vTab.catching { rename(newName) }
}

/**
 * Common logic for [VTabSavepointCallback], [VTabReleaseCallback] and [VTabRollbackToCallback].
 */
private inline fun nestedTransaction(crossinline block: VirtualTable.(Int) -> Unit) =
    SqliteVTabNestedTransactionCallback { vTab: VTab, id ->
        vTab.catching { block(id) }
    }

/**
 * Invokes [VirtualTable.savepoint].
 */
internal val VTabSavepointCallback = nestedTransaction(VirtualTable::savepoint)

/**
 * Invokes [VirtualTable.release].
 */
internal val VTabReleaseCallback = nestedTransaction(VirtualTable::release)

/**
 * Invokes [VirtualTable.rollbackTo].
 */
internal val VTabRollbackToCallback = nestedTransaction(VirtualTable::rollbackTo)

/**
 * Invokes [VirtualTable.integrity].
 */
internal val VTabIntegrityCallback =
    SqliteVTabIntegrityCallback { vTab: VTab, schema, tableName, flags ->
        vTab.table.runCatchingSQLiteException({ error ->
            failure(error.message, error.result)
        }) {
            success(VirtualTableIntegrityScopeImpl().use { scope ->
                scope.apply { integrity(schema, tableName, flags) }.message
            })
        }
    }