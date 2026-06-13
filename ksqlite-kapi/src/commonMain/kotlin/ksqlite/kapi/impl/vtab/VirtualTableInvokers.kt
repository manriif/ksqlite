package ksqlite.kapi.impl.vtab

import ksqlite.capi.sqlite3_result_text
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.callbacks.Sqlite3VTabBestIndexCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCloseCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabColumnCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCreateOrConnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabDestroyOrDisconnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabEofCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabFilterCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabIntegrityCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabNestedTransactionCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabNextCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabOpenCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRenameCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRowidCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabTransactionCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabUpdateCallback
import ksqlite.kapi.Connection
import ksqlite.kapi.impl.functions.ScalarFunctionFuncInvoker
import ksqlite.kapi.impl.helpers.AutoCloser
import ksqlite.kapi.impl.helpers.ContextClosableScope
import ksqlite.kapi.impl.helpers.runCatchingSQLiteException
import ksqlite.kapi.impl.retrieveConnection
import ksqlite.kapi.value.toProtectedValues
import ksqlite.kapi.vtab.VirtualTable
import ksqlite.kapi.vtab.VirtualTableCursor
import ksqlite.kapi.vtab.VirtualTableModule

/**
 * Common logic for [VTabCreateInvoker] and [VTabConnectInvoker].
 */
private inline fun <Module : VirtualTableModule> createOrConnect(
    crossinline block: Module.(
        scope: VirtualTableCreateOrConnectScope,
        connection: Connection,
        arguments: Array<String>
    ) -> VirtualTable
) = Sqlite3VTabCreateOrConnectCallback<Module, VTab> { db, module, arguments ->
    module.runCatchingSQLiteException({ failure(it.message) }) {
        val connection = retrieveConnection(db)

        val table = VirtualTableCreateOrConnectScope(db).use { scope ->
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
internal val VTabCreateInvoker =
    createOrConnect<VirtualTableModule.Regular> { scope, connection, arguments ->
        scope.create(connection, arguments)
    }

/**
 * Invokes [VirtualTableModule.connect]
 */
internal val VTabConnectInvoker =
    createOrConnect<VirtualTableModule> { scope, connection, arguments ->
        scope.connect(connection, arguments)
    }

/**
 * Invokes [VirtualTable.bestIndex].
 */
internal val VTabBestIndexInvoker = Sqlite3VTabBestIndexCallback { vTab: VTab, info ->
    vTab.catching {
        VirtualTableBestIndexScopeImpl(info).use { it.bestIndex(info) }
    }
}

/**
 * Common logic for [VTabCloseInvoker] and [VTabDisconnectInvoker].
 */
private inline fun disconnectOrDestroy(crossinline block: VirtualTable.() -> Unit) =
    Sqlite3VTabDestroyOrDisconnectCallback { vTab: VTab ->
        try {
            vTab.catching(block)
        } finally {
            vTab.table.parent = null
        }
    }

/**
 * Invokes [VirtualTable.disconnect].
 */
internal val VTabDisconnectInvoker = disconnectOrDestroy(VirtualTable::disconnect)

/**
 * Invokes [VirtualTable.destroy].
 */
internal val VTabDestroyInvoker = disconnectOrDestroy(VirtualTable::destroy)

/**
 * Invokes [VirtualTable.open].
 */
internal val VTabOpenInvoker = Sqlite3VTabOpenCallback { vTab: VTab ->
    vTab.catching(::failure) {
        val cursor = open()
        val vTabCursor = VTabCursor(cursor, vTab)
        success(vTabCursor)
    }
}

/**
 * Invokes [VirtualTableCursor.close].
 */
internal val VTabCloseInvoker = Sqlite3VTabCloseCallback { cursor: VTabCursor ->
    try {
        cursor.catching(VirtualTableCursor::close)
    } finally {
        cursor.cursor.close()
    }
}

/**
 * Invokes [VirtualTableCursor.eof].
 */
internal val VTabEofInvoker = Sqlite3VTabEofCallback { cursor: VTabCursor ->
    cursor.catching({ -1 }) { if (eof()) 1 else 0 }
}

/**
 * Invokes [VirtualTableCursor.filter].
 */
internal val VTabFilterInvoker =
    Sqlite3VTabFilterCallback { cursor: VTabCursor, idxNum, idxStr, arguments ->
        cursor.catching {
            VirtualTableFilterScopeImpl().use { scope ->
                scope.filter(idxNum, idxStr, arguments.toProtectedValues(scope))
            }
        }
    }

/**
 * Invokes [VirtualTableCursor.next].
 */
internal val VTabNextInvoker = Sqlite3VTabNextCallback { cursor: VTabCursor ->
    cursor.catching(VirtualTableCursor::next)
}

/**
 * Invokes [VirtualTableCursor.column].
 */
internal val VTabColumnInvoker = Sqlite3VTabColumnCallback { cursor: VTabCursor, context, index ->
    cursor.cursor.runCatchingSQLiteException({ error ->
        // sqlite3_result_text() must be used here to set the error message according to SQLite
        sqlite3_result_text(context, error.message)
        error.result
    }) {
        ContextClosableScope(context).use { scope ->
            VirtualTableColumnScopeImpl(scope).column(index)
        }

        Sqlite3Result.OK
    }
}

/**
 * Invokes [VirtualTableCursor.rowid].
 */
internal val VTabRowidInvoker = Sqlite3VTabRowidCallback { cursor: VTabCursor ->
    cursor.catching(::failure) { success(rowid()) }
}

/**
 * Invokes [VirtualTable.update].
 */
internal val VTabUpdateInvoker = Sqlite3VTabUpdateCallback { vTab: VTab, arguments ->
    vTab.catching(::failure) {
        VirtualTableUpdateScopeImpl(vTab.db).use { scope ->
            success(scope.update(arguments.toProtectedValues(scope)))
        }
    }
}

/**
 * Invokes [VirtualTable.findFunction].
 */
internal val VTabFindFunctionInvoker = Sqlite3VTabFindFunctionCallback { vTab: VTab, argc, name ->
    vTab.catching({ doNotOverload() }) {
        VirtualTableFindFunctionScopeImpl().use { scope ->
            scope.findFunction(name, argc)?.let { function ->
                scope.customCode
                    ?.let { overload(it, function, ScalarFunctionFuncInvoker, AutoCloser) }
                    ?: overload(function, ScalarFunctionFuncInvoker, AutoCloser)
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
 * Common logic for [VTabBeginInvoker], [VTabSyncInvoker], [VTabCommitInvoker] and
 * [VTabRollbackInvoker].
 */
private inline fun transaction(crossinline block: VirtualTable.() -> Unit) =
    Sqlite3VTabTransactionCallback { vTab: VTab ->
        vTab.catching(block)
    }

/**
 * Invokes [VirtualTable.begin].
 */
internal val VTabBeginInvoker = transaction(VirtualTable::begin)

/**
 * Invokes [VirtualTable.sync].
 */
internal val VTabSyncInvoker = transaction(VirtualTable::sync)

/**
 * Invokes [VirtualTable.commit].
 */
internal val VTabCommitInvoker = transaction(VirtualTable::commit)

/**
 * Invokes [VirtualTable.rollback].
 */
internal val VTabRollbackInvoker = transaction(VirtualTable::rollback)

/**
 * Invokes [VirtualTable.rename].
 */
internal val VTabRenameInvoker = Sqlite3VTabRenameCallback { vTab: VTab, newName ->
    vTab.catching { rename(newName) }
}

/**
 * Common logic for [VTabSavepointInvoker], [VTabReleaseInvoker] and [VTabRollbackToInvoker].
 */
private inline fun nestedTransaction(crossinline block: VirtualTable.(Int) -> Unit) =
    Sqlite3VTabNestedTransactionCallback { vTab: VTab, id ->
        vTab.catching { block(id) }
    }

/**
 * Invokes [VirtualTable.savepoint].
 */
internal val VTabSavepointInvoker = nestedTransaction(VirtualTable::savepoint)

/**
 * Invokes [VirtualTable.release].
 */
internal val VTabReleaseInvoker = nestedTransaction(VirtualTable::release)

/**
 * Invokes [VirtualTable.rollbackTo].
 */
internal val VTabRollbackToInvoker = nestedTransaction(VirtualTable::rollbackTo)

/**
 * Invokes [VirtualTable.integrity].
 */
internal val VTabIntegrityInvoker =
    Sqlite3VTabIntegrityCallback { vTab: VTab, schema, tableName, flags ->
        vTab.table.runCatchingSQLiteException({ error ->
            failure(error.message, error.result)
        }) {
            success(VirtualTableIntegrityScopeImpl().use { scope ->
                scope.apply { integrity(schema, tableName, flags) }.message
            })
        }
    }