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

import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.sqlite3_result_text
import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.callbacks.SqliteVtabBestIndexCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabCloseCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabColumnCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabCreateOrConnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabDestroyOrDisconnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabEofCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabFilterCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabIntegrityCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabNestedTransactionCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabNextCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabOpenCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabRenameCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabRowidCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabTransactionCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabUpdateCallback
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.function.ScalarFunctionFuncCallback
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
 * Common logic for [VtabCreateCallback] and [VtabConnectCallback].
 */
private inline fun <Module : VirtualTableModule> createOrConnect(
    crossinline block: Module.(
        scope: VirtualTableCreateOrConnectScope,
        connection: DatabaseConnection,
        arguments: Array<String>
    ) -> VirtualTable
) = SqliteVtabCreateOrConnectCallback<Module, Vtab> { db, module, arguments ->
    module.runCatchingSQLiteException({ failure(it.message) }) {
        val connection = sqliteRequireConnection(db)

        val table = VirtualTableCreateOrConnectScopeImpl(db).use { scope ->
            block(module, scope, connection, arguments)
        }

        val vTab = Vtab(table, db)
        table.parent = vTab

        success(vTab)
    }
}

/**
 * Invokes [VirtualTableModule.Regular.create]
 */
internal val VtabCreateCallback =
    createOrConnect<VirtualTableModule.Regular> { scope, connection, arguments ->
        scope.create(connection, arguments)
    }

/**
 * Invokes [VirtualTableModule.connect]
 */
internal val VtabConnectCallback =
    createOrConnect<VirtualTableModule> { scope, connection, arguments ->
        scope.connect(connection, arguments)
    }

/**
 * Invokes [VirtualTable.bestIndex].
 */
internal val VtabBestIndexCallback = SqliteVtabBestIndexCallback { vTab: Vtab, info ->
    vTab.catching {
        VirtualTableBestIndexScopeImpl(info).use { it.bestIndex(info) }
    }
}

/**
 * Common logic for [VtabCloseCallback] and [VtabDisconnectCallback].
 */
private inline fun disconnectOrDestroy(crossinline block: VirtualTable.() -> Unit) =
    SqliteVtabDestroyOrDisconnectCallback { vTab: Vtab ->
        try {
            vTab.catching(block)
        } finally {
            vTab.table.parent = null
        }
    }

/**
 * Invokes [VirtualTable.disconnect].
 */
internal val VtabDisconnectCallback = disconnectOrDestroy(VirtualTable::disconnect)

/**
 * Invokes [VirtualTable.destroy].
 */
internal val VtabDestroyCallback = disconnectOrDestroy(VirtualTable::destroy)

/**
 * Invokes [VirtualTable.open].
 */
internal val VtabOpenCallback = SqliteVtabOpenCallback { vTab: Vtab ->
    vTab.catching(::failure) {
        val cursor = open()
        val vTabCursor = VtabCursor(cursor, vTab)
        success(vTabCursor)
    }
}

/**
 * Invokes [VirtualTableCursor.close].
 */
internal val VtabCloseCallback = SqliteVtabCloseCallback { cursor: VtabCursor ->
    try {
        cursor.catching(VirtualTableCursor::close)
    } finally {
        cursor.cursor.close()
    }
}

/**
 * Invokes [VirtualTableCursor.eof].
 */
internal val VtabEofCallback = SqliteVtabEofCallback { cursor: VtabCursor ->
    cursor.catching({ -1 }) { if (eof()) 1 else 0 }
}

/**
 * Invokes [VirtualTableCursor.filter].
 */
internal val VtabFilterCallback =
    SqliteVtabFilterCallback { cursor: VtabCursor, idxNum, idxStr, arguments ->
        cursor.catching {
            VirtualTableFilterScopeImpl().use { scope ->
                scope.filter(idxNum, idxStr, arguments.toProtectedValues(scope))
            }
        }
    }

/**
 * Invokes [VirtualTableCursor.next].
 */
internal val VtabNextCallback = SqliteVtabNextCallback { cursor: VtabCursor ->
    cursor.catching(VirtualTableCursor::next)
}

/**
 * Invokes [VirtualTableCursor.column].
 */
internal val VtabColumnCallback = SqliteVtabColumnCallback { cursor: VtabCursor, context, index ->
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
internal val VtabRowidCallback = SqliteVtabRowidCallback { cursor: VtabCursor ->
    cursor.catching(::failure) { success(rowid()) }
}

/**
 * Invokes [VirtualTable.update].
 */
internal val VtabUpdateCallback = SqliteVtabUpdateCallback { vTab: Vtab, arguments ->
    vTab.catching(::failure) {
        VirtualTableUpdateScopeImpl(vTab.db).use { scope ->
            success(scope.update(arguments.toProtectedValues(scope)))
        }
    }
}

/**
 * Invokes [VirtualTable.findFunction].
 */
internal val VtabFindFunctionCallback = SqliteVtabFindFunctionCallback { vTab: Vtab, argc, name ->
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
 * Common logic for [VtabBeginCallback], [VtabSyncCallback], [VtabCommitCallback] and
 * [VtabRollbackCallback].
 */
private inline fun transaction(crossinline block: VirtualTable.() -> Unit) =
    SqliteVtabTransactionCallback { vTab: Vtab ->
        vTab.catching(block)
    }

/**
 * Invokes [VirtualTable.begin].
 */
internal val VtabBeginCallback = transaction(VirtualTable::begin)

/**
 * Invokes [VirtualTable.sync].
 */
internal val VtabSyncCallback = transaction(VirtualTable::sync)

/**
 * Invokes [VirtualTable.commit].
 */
internal val VtabCommitCallback = transaction(VirtualTable::commit)

/**
 * Invokes [VirtualTable.rollback].
 */
internal val VtabRollbackCallback = transaction(VirtualTable::rollback)

/**
 * Invokes [VirtualTable.rename].
 */
internal val VtabRenameCallback = SqliteVtabRenameCallback { vTab: Vtab, newName ->
    vTab.catching { rename(newName) }
}

/**
 * Common logic for [VtabSavepointCallback], [VtabReleaseCallback] and [VtabRollbackToCallback].
 */
private inline fun nestedTransaction(crossinline block: VirtualTable.(Int) -> Unit) =
    SqliteVtabNestedTransactionCallback { vTab: Vtab, id ->
        vTab.catching { block(id) }
    }

/**
 * Invokes [VirtualTable.savepoint].
 */
internal val VtabSavepointCallback = nestedTransaction(VirtualTable::savepoint)

/**
 * Invokes [VirtualTable.release].
 */
internal val VtabReleaseCallback = nestedTransaction(VirtualTable::release)

/**
 * Invokes [VirtualTable.rollbackTo].
 */
internal val VtabRollbackToCallback = nestedTransaction(VirtualTable::rollbackTo)

/**
 * Invokes [VirtualTable.integrity].
 */
internal val VtabIntegrityCallback =
    SqliteVtabIntegrityCallback { vTab: Vtab, schema, tableName, flags ->
        vTab.table.runCatchingSQLiteException({ error ->
            failure(error.message, error.result)
        }) {
            success(VirtualTableIntegrityScopeImpl().use { scope ->
                scope.apply { integrity(schema, tableName, flags) }.message
            })
        }
    }