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
package ksqlite.kapi.database

import co.touchlab.stately.collections.ConcurrentMutableSet
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_autovacuum_pages
import ksqlite.capi.sqlite3_blob
import ksqlite.capi.sqlite3_blob_open
import ksqlite.capi.sqlite3_busy_handler
import ksqlite.capi.sqlite3_busy_timeout
import ksqlite.capi.sqlite3_changes64
import ksqlite.capi.sqlite3_close_v2
import ksqlite.capi.sqlite3_collation_needed
import ksqlite.capi.sqlite3_commit_hook
import ksqlite.capi.sqlite3_create_collation_v2
import ksqlite.capi.sqlite3_create_function_v2
import ksqlite.capi.sqlite3_create_module_v2
import ksqlite.capi.sqlite3_create_window_function
import ksqlite.capi.sqlite3_db_cacheflush
import ksqlite.capi.sqlite3_db_filename
import ksqlite.capi.sqlite3_db_name
import ksqlite.capi.sqlite3_db_readonly
import ksqlite.capi.sqlite3_db_release_memory
import ksqlite.capi.sqlite3_db_status64
import ksqlite.capi.sqlite3_deserialize
import ksqlite.capi.sqlite3_drop_modules
import ksqlite.capi.sqlite3_exec
import ksqlite.capi.sqlite3_extended_result_codes
import ksqlite.capi.sqlite3_get_autocommit
import ksqlite.capi.sqlite3_interrupt
import ksqlite.capi.sqlite3_is_interrupted
import ksqlite.capi.sqlite3_key_v2
import ksqlite.capi.sqlite3_last_insert_rowid
import ksqlite.capi.sqlite3_limit
import ksqlite.capi.sqlite3_prepare_v3
import ksqlite.capi.sqlite3_preupdate_hook
import ksqlite.capi.sqlite3_progress_handler
import ksqlite.capi.sqlite3_rekey_v2
import ksqlite.capi.sqlite3_rollback_hook
import ksqlite.capi.sqlite3_serialize
import ksqlite.capi.sqlite3_set_authorizer
import ksqlite.capi.sqlite3_set_last_insert_rowid
import ksqlite.capi.sqlite3_snapshot
import ksqlite.capi.sqlite3_snapshot_get
import ksqlite.capi.sqlite3_snapshot_open
import ksqlite.capi.sqlite3_snapshot_recover
import ksqlite.capi.sqlite3_stmt
import ksqlite.capi.sqlite3_table_column_metadata
import ksqlite.capi.sqlite3_total_changes64
import ksqlite.capi.sqlite3_trace_v2
import ksqlite.capi.sqlite3_txn_state
import ksqlite.capi.sqlite3_update_hook
import ksqlite.capi.vtab.callbacks.SqliteVtabConnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVtabCreateCallback
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.blob.BlobImpl
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.ReadableBuffer
import ksqlite.kapi.cipher.CipherConfigurationImpl
import ksqlite.kapi.cipher.CipherDataImpl
import ksqlite.kapi.function.AggregateFunction
import ksqlite.kapi.function.AggregateFunctionFinalCallback
import ksqlite.kapi.function.AggregateFunctionStepCallback
import ksqlite.kapi.function.ScalarFunction
import ksqlite.kapi.function.ScalarFunctionFuncCallback
import ksqlite.kapi.function.WindowFunction
import ksqlite.kapi.function.WindowFunctionInverseCallback
import ksqlite.kapi.function.WindowFunctionValueCallback
import ksqlite.kapi.helpers.AutoCloser
import ksqlite.kapi.helpers.DelegatingAtomicCloseableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.helpers.usingParams
import ksqlite.kapi.snapshot.Snapshot
import ksqlite.kapi.snapshot.SnapshotImpl
import ksqlite.kapi.statement.PreparedStatement
import ksqlite.kapi.statement.PreparedStatementImpl
import ksqlite.kapi.throwSQLiteException
import ksqlite.kapi.value.Status
import ksqlite.kapi.value.StatusImpl
import ksqlite.kapi.vfs.FileName
import ksqlite.kapi.vfs.FileNameImpl
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.kapi.vtab.VirtualTableModuleDestructor
import ksqlite.kapi.vtab.VirtualTableOptionalFunction
import ksqlite.kapi.vtab.Vtab
import ksqlite.kapi.vtab.VtabBeginCallback
import ksqlite.kapi.vtab.VtabBestIndexCallback
import ksqlite.kapi.vtab.VtabCloseCallback
import ksqlite.kapi.vtab.VtabColumnCallback
import ksqlite.kapi.vtab.VtabCommitCallback
import ksqlite.kapi.vtab.VtabConnectCallback
import ksqlite.kapi.vtab.VtabCreateCallback
import ksqlite.kapi.vtab.VtabDestroyCallback
import ksqlite.kapi.vtab.VtabDisconnectCallback
import ksqlite.kapi.vtab.VtabEofCallback
import ksqlite.kapi.vtab.VtabFilterCallback
import ksqlite.kapi.vtab.VtabFindFunctionCallback
import ksqlite.kapi.vtab.VtabIntegrityCallback
import ksqlite.kapi.vtab.VtabNextCallback
import ksqlite.kapi.vtab.VtabOpenCallback
import ksqlite.kapi.vtab.VtabReleaseCallback
import ksqlite.kapi.vtab.VtabRenameCallback
import ksqlite.kapi.vtab.VtabRollbackCallback
import ksqlite.kapi.vtab.VtabRollbackToCallback
import ksqlite.kapi.vtab.VtabRowidCallback
import ksqlite.kapi.vtab.VtabSavepointCallback
import ksqlite.kapi.vtab.VtabSyncCallback
import ksqlite.kapi.vtab.VtabUpdateCallback
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqlitePrepareFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteSerializeFlag
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.SqliteTransactionState
import ksqlite.types.vtab.SqliteModuleVersion

internal class DatabaseConnectionImpl(
    override val db: sqlite3,
    private val listener: Listener,
) : DatabaseConnection() {

    private val scope = DelegatingAtomicCloseableScope(::closeConnection)
    private val closeables = ConcurrentMutableSet<AutoCloseable>()

    override val config = DatabaseConnectionConfigurationImpl(db, scope)
    override val cipherConfig = CipherConfigurationImpl(db, scope)
    override val cipherData = CipherDataImpl(db, scope)
    override val lastError = LastErrorImpl(db, scope)
    override val fileControl = FileControlImpl(db, scope)
    override val wal = WriteAheadLogImpl(db, scope)

    override val changes: Long
        get() = scope.notClosed { sqlite3_changes64(db) }

    override val isAutocommit: Boolean
        get() = scope.notClosed { sqlite3_get_autocommit(db) != 0 }

    override val isInterrupted: Boolean
        get() = scope.notClosed { sqlite3_is_interrupted(db) == 1 }

    override var lastInsertRowid: Long
        get() = scope.notClosed { sqlite3_last_insert_rowid(db) }
        set(value) = scope.notClosed { sqlite3_set_last_insert_rowid(db, value) }

    override val totalChanges: Long
        get() = scope.notClosed { sqlite3_total_changes64(db) }

    /**
     * Updates a [Handler], invoking [clear] to clear any exiting handler if [handler] is `null` or
     * invoking [set] to replace existing one with non `null` [handler] instance.
     */
    private inline fun <Handler> updateHandlerWithResult(
        handler: Handler?,
        clear: () -> SqliteResultCode,
        set: (Handler) -> SqliteResultCode
    ) = scope.notClosed { sqliteResultCheck(handler?.let(set) ?: clear()) }

    /**
     * Updates a [Handler], invoking [clear] to clear any exiting handler if [handler] is `null` or
     * invoking [set] to replace existing one with non `null` [handler] instance.
     */
    private inline fun <Handler> updateHandler(
        handler: Handler?,
        clear: () -> Unit,
        set: (Handler) -> Unit
    ) = scope.notClosed { handler?.let(set) ?: clear() }

    /**
     * Closes the connection and frees resources.
     */
    private fun closeConnection() {
        db.resultCheck(sqlite3_close_v2(db))

        closeables
            .onEach { it.close() }
            .clear()

        listener.onConnectionClosed(db)
    }

    override fun close() = scope.close()

    override fun setAutovacuumPages(handler: AutovacuumPages?) = updateHandlerWithResult(
        handler = handler,
        clear = { sqlite3_autovacuum_pages(db, null, null, null) },
        set = { sqlite3_autovacuum_pages(db, it, AutoCloser, AutovacuumPagesCallback) }
    )

    override fun openBlob(
        tableName: String,
        columnName: String,
        rowid: Long,
        database: String,
        flags: SqliteBlobOpenFlag
    ): Blob = scope.notClosed {
        BlobImpl(db, usingParam(sqlite3_blob.OutputParam()) { outBlob ->
            sqliteResultCheck(
                sqlite3_blob_open(
                    db = db,
                    database = database,
                    tableName = tableName,
                    columnName = columnName,
                    rowid = rowid,
                    flags = flags,
                    outBlob = outBlob
                )
            )
        })
    }

    override fun setBusyHandler(handler: BusyHandler?) = updateHandlerWithResult(
        handler = handler,
        clear = { sqlite3_busy_handler(db, null, null) },
        set = { sqlite3_busy_handler(db, it, BusyHandlerCallback) }
    )

    override fun setBusyTimeout(millis: Int) =
        scope.notClosed { sqliteResultCheck(sqlite3_busy_timeout(db, millis)) }

    override fun setCollationNeeded(handler: CollationNeeded?) = updateHandlerWithResult(
        handler = handler,
        clear = { sqlite3_collation_needed(db, null, null) },
        set = { sqlite3_collation_needed(db, it, CollationNeededCallback) }
    )

    override fun setCommitHook(handler: CommitHook?) = updateHandler(
        handler = handler,
        clear = { sqlite3_commit_hook(db, null, null) },
        set = { sqlite3_commit_hook(db, it, CommitHookCallback) }
    )

    override fun createCollation(
        name: String,
        encoding: SqliteTextEncoding.CreateCollation,
        collation: Collation
    ) = scope.notClosed {
        db.resultCheck(
            sqlite3_create_collation_v2(
                db = db,
                name = name,
                encoding = encoding,
                appData = collation,
                destroy = AutoCloser,
                callback = CollationCallback
            ),
            cleanup = collation::close
        )
    }

    override fun deleteCollation(
        name: String,
        encoding: SqliteTextEncoding.CreateCollation
    ) = scope.notClosed {
        db.resultCheck(
            sqlite3_create_collation_v2(
                db = db,
                name = name,
                encoding = encoding,
                appData = null,
                destroy = null,
                callback = null
            )
        )
    }

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: ScalarFunction
    ) = scope.notClosed {
        db.resultCheck(
            sqlite3_create_function_v2(
                db = db,
                name = name,
                nArg = argumentCount,
                encoding = encoding,
                appData = function,
                func = ScalarFunctionFuncCallback,
                step = null,
                final = null,
                destroy = AutoCloser
            )
        )
    }

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: AggregateFunction
    ) = scope.notClosed {
        db.resultCheck(
            sqlite3_create_function_v2(
                db = db,
                name = name,
                nArg = argumentCount,
                encoding = encoding,
                appData = function,
                func = null,
                step = AggregateFunctionStepCallback,
                final = AggregateFunctionFinalCallback,
                destroy = AutoCloser
            )
        )
    }

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        function: WindowFunction
    ) = scope.notClosed {
        db.resultCheck(
            sqlite3_create_window_function(
                db = db,
                name = name,
                nArg = argumentCount,
                encoding = encoding,
                appData = function,
                step = AggregateFunctionStepCallback,
                final = AggregateFunctionFinalCallback,
                inverse = WindowFunctionInverseCallback,
                value = WindowFunctionValueCallback,
                destroy = AutoCloser
            )
        )
    }

    override fun deleteFunction(
        name: String,
        argumentCount: Int,
        encoding: SqliteFunctionTextEncoding,
        isWindowFunction: Boolean
    ) = scope.notClosed {
        db.resultCheck(
            if (isWindowFunction) {
                sqlite3_create_window_function(
                    db = db,
                    name = name,
                    nArg = argumentCount,
                    encoding = encoding,
                    appData = null,
                    step = null,
                    final = null,
                    inverse = null,
                    value = null,
                    destroy = null
                )
            } else {
                sqlite3_create_function_v2(
                    db = db,
                    name = name,
                    nArg = argumentCount,
                    encoding = encoding,
                    appData = null,
                    func = null,
                    step = null,
                    final = null,
                    destroy = null
                )
            }
        )
    }

    /**
     * Installs a virtual table module.
     */
    private fun <Module : VirtualTableModule> Module.install(
        name: String,
        version: SqliteModuleVersion,
        create: SqliteVtabCreateCallback<in Module, Vtab>?,
        connect: SqliteVtabConnectCallback<in Module, Vtab>,
    ): Unit = scope.notClosed {
        val optionalFunctions = optionalFunctions()

        val sqliteModule = sqlite3_module(
            version = version,
            create = create,
            connect = connect,
            bestIndex = VtabBestIndexCallback,
            disconnect = VtabDisconnectCallback,
            destroy = VtabDestroyCallback,
            open = VtabOpenCallback,
            close = VtabCloseCallback,
            filter = VtabFilterCallback,
            next = VtabNextCallback,
            eof = VtabEofCallback,
            column = VtabColumnCallback,
            rowid = VtabRowidCallback,
            update = VtabUpdateCallback
                .takeIf { VirtualTableOptionalFunction.Update in optionalFunctions },
            findFunction = VtabFindFunctionCallback
                .takeIf { VirtualTableOptionalFunction.FindFunction in optionalFunctions },
            begin = VtabBeginCallback
                .takeIf { VirtualTableOptionalFunction.Begin in optionalFunctions },
            sync = VtabSyncCallback
                .takeIf { VirtualTableOptionalFunction.Sync in optionalFunctions },
            commit = VtabCommitCallback
                .takeIf { VirtualTableOptionalFunction.Commit in optionalFunctions },
            rollback = VtabRollbackCallback
                .takeIf { VirtualTableOptionalFunction.Rollback in optionalFunctions },
            rename = VtabRenameCallback
                .takeIf { VirtualTableOptionalFunction.Rename in optionalFunctions },
            savepoint = VtabSavepointCallback
                .takeIf { VirtualTableOptionalFunction.Savepoint in optionalFunctions },
            release = VtabReleaseCallback
                .takeIf { VirtualTableOptionalFunction.Release in optionalFunctions },
            rollbackTo = VtabRollbackToCallback
                .takeIf { VirtualTableOptionalFunction.RollbackTo in optionalFunctions },
            integrity = VtabIntegrityCallback
                .takeIf { VirtualTableOptionalFunction.Integrity in optionalFunctions },
        )

        moduleLock.withLock {
            check(module == null) {
                "The module is already installed on a database connection"
            }

            module = sqliteModule
        }

        // TODO SQLite is a bit vague regarding when it decide to clear the appData, we
        //  must ensure that the sqlite3_module itself is no longer required too when
        //  the appData are destroyed. For now we assume that the destructor is invoked
        //  when the module is dropped or replaced
        db.resultCheck(
            sqlite3_create_module_v2(
                db = db,
                name = name,
                module = sqliteModule,
                appData = this,
                destroy = VirtualTableModuleDestructor
            )
        )
    }

    override fun createModule(
        name: String,
        version: SqliteModuleVersion,
        module: VirtualTableModule.Regular
    ) = module.install(
        name = name,
        version = version,
        create = VtabCreateCallback,
        connect = VtabConnectCallback
    )

    override fun createModule(
        name: String,
        version: SqliteModuleVersion,
        module: VirtualTableModule.Eponymous
    ) = module.install(
        name = name,
        version = version,
        create = VtabConnectCallback,
        connect = VtabConnectCallback
    )

    override fun createModule(
        name: String,
        version: SqliteModuleVersion,
        module: VirtualTableModule.EponymousOnly
    ) = module.install(
        name = name,
        version = version,
        create = null,
        connect = VtabConnectCallback
    )

    override fun deleteModule(name: String) = scope.notClosed {
        db.resultCheck(
            sqlite3_create_module_v2(
                db = db,
                name = name,
                module = null,
                appData = null,
                destroy = null
            )
        )
    }

    override fun deleteModules(keep: Set<String>) =
        scope.notClosed { db.resultCheck(sqlite3_drop_modules(db, keep.toTypedArray())) }

    override fun flushCache() =
        scope.notClosed { sqliteResultCheck(sqlite3_db_cacheflush(db)) }

    override fun getFileName(database: String): FileName? =
        scope.notClosed { sqlite3_db_filename(db, database)?.let(::FileNameImpl) }

    override fun getName(index: Int): String? =
        scope.notClosed { sqlite3_db_name(db, index) }

    override fun isReadOnly(database: String): Boolean = scope.notClosed {
        when (sqlite3_db_readonly(db, database)) {
            READWRITE -> false
            READONLY -> true
            UNKNOWN_DATABASE ->
                throwSQLiteException("No database named $database on this database connection")
        }
    }

    override fun releaseMemory() =
        scope.notClosed { sqliteResultCheck(sqlite3_db_release_memory(db)) }

    override fun getStatus(
        option: SqliteDbStatusOption,
        reset: Boolean
    ): Status = scope.notClosed {
        usingParams(
            param1 = Int64OutputParam(-1),
            param2 = Int64OutputParam(-1),
            transform = ::StatusImpl
        ) { outCur, outHighwater ->
            sqliteResultCheck(
                sqlite3_db_status64(
                    db = db,
                    option = option,
                    outCurrent = outCur,
                    outHighwater = outHighwater,
                    resetFlag = if (reset) 1 else 0,
                )
            )
        }
    }

    override fun deserialize(
        serializedDatabase: Buffer,
        database: String?,
        databaseSize: Long,
        bufferSize: Long,
        flags: SqliteDeserializeFlag?
    ): Unit = scope.notClosed {
        var freeOnClose = false

        // Remove the FREEONCLOSE flag to prevent double free of the buffer (SQLite + Kotlin) and
        // free it on Kotlin  side only
        val updatedFlags = flags?.let { flags ->
            if (SqliteDeserializeFlag.FREEONCLOSE in flags) {
                freeOnClose = true
                flags without SqliteDeserializeFlag.FREEONCLOSE
            } else {
                flags
            }
        }

        sqliteResultCheck(
            sqlite3_deserialize(
                db = db,
                database = database,
                buffer = serializedDatabase.buffer,
                dbSize = databaseSize,
                bufferSize = bufferSize,
                flags = updatedFlags,
            )
        ) {
            // Keep SQLite behavior and free the buffer
            if (freeOnClose) {
                serializedDatabase.close()
            }
        }

        // Keep a reference to prevent someone from closing the buffer while it is being used
        // SQLite needs the buffer until the connection is closed
        val destructor = serializedDatabase.reference { buffer ->
            if (freeOnClose) {
                buffer.close()
            }
        }

        closeables.add(AutoCloseable {
            destructor.apply(serializedDatabase.buffer)
        })
    }

    override fun setExtendedResultCodesEnabled(enabled: Boolean) = scope.notClosed {
        sqliteResultCheck(sqlite3_extended_result_codes(db, if (enabled) 1 else 0))
    }

    override fun execute(
        sql: String,
        callback: Exec?
    ) = scope.notClosed {
        val outError = Utf8OutputParam()

        val result = if (callback != null) {
            sqlite3_exec(
                db = db,
                sql = sql,
                outErrorMessage = outError,
                appData = callback,
                callback = ExecCallback
            )
        } else {
            sqlite3_exec(
                db = db,
                sql = sql,
                outErrorMessage = outError,
                appData = null,
                callback = null
            )
        }

        outError.value?.let { error ->
            throwSQLiteException(
                message = error,
                result = result as? SqliteResultCode.Failure ?: SqliteResultCode.ERROR
            )
        }

        sqliteResultCheck(result)
    }

    override fun interrupt() = scope.notClosed { sqlite3_interrupt(db) }

    override fun setKey(key: ByteArray, size: Int, database: String) =
        scope.notClosed { sqliteResultCheck(sqlite3_key_v2(db, database, key, size)) }

    override fun getLimit(limit: SqliteRuntimeLimit): Int =
        scope.notClosed { sqlite3_limit(db, limit, -1) }

    override fun setLimit(limit: SqliteRuntimeLimit, value: Int) = scope.notClosed {
        val _ = sqlite3_limit(db, limit, value)
    }

    override fun prepare(
        sql: String,
        flags: SqlitePrepareFlag?
    ): PreparedStatement = scope.notClosed {
        val stmt = usingParam(sqlite3_stmt.OutputParam()) { outStmt ->
            sqliteResultCheck(sqlite3_prepare_v3(db, sql, flags, outStmt))
        }

        val statement = PreparedStatementImpl(
            connection = this,
            parentScope = scope,
            stmt = stmt,
            listener = listener::onStatementClosed
        )

        listener.onStatementCreated(stmt, statement)
        return statement
    }

    override fun setPreupdateHook(handler: PreupdateHook?) = updateHandler(
        handler = handler,
        clear = { sqlite3_preupdate_hook(db, null, null) },
        set = { sqlite3_preupdate_hook(db, it, PreupdateHookCallback) }
    )

    override fun setProgressHandler(
        operationCount: Int,
        handler: ProgressHandler?
    ) = if (operationCount > 0) {
        updateHandler(
            handler = handler,
            clear = { sqlite3_progress_handler(db, operationCount, null, null) },
            set = { sqlite3_progress_handler(db, operationCount, it, ProgressHandlerCallback) }
        )
    } else {
        // Non-positive operationCount simply disable the handler according to SQLite
        scope.notClosed {
            sqlite3_progress_handler(db, operationCount, null, null)
        }
    }

    override fun setReKey(key: ByteArray, size: Int, database: String) =
        scope.notClosed { sqliteResultCheck(sqlite3_rekey_v2(db, database, key, size)) }

    override fun setRollbackHook(handler: RollbackHook?) = updateHandler(
        handler = handler,
        clear = { sqlite3_rollback_hook(db, null, null) },
        set = { sqlite3_rollback_hook(db, it, RollbackHookCallback) }
    )

    override fun serialize(
        flags: SqliteSerializeFlag?,
        database: String?
    ): SerializeResult = scope.notClosed {
        when (val result = sqlite3_serialize(db, database, flags)) {
            is Failure -> SerializeResult.Failure(result.databaseSize)
            is Immutable -> SerializeResult.Immutable(ReadableBuffer(result.buffer))
            is Mutable -> SerializeResult.Mutable(Buffer(result.buffer))
        }
    }

    override fun setAuthorizer(handler: Authorizer?) = updateHandlerWithResult(
        handler = handler,
        clear = { sqlite3_set_authorizer(db, null, null) },
        set = { sqlite3_set_authorizer(db, it, AuthorizerCallback) }
    )

    override fun createSnapshot(database: String?): Snapshot = scope.notClosed {
        SnapshotImpl(usingParam(sqlite3_snapshot.OutputParam()) { outSnapshot ->
            sqliteResultCheck(sqlite3_snapshot_get(db, database, outSnapshot))
        })
    }

    override fun openSnapshot(
        snapshot: Snapshot,
        database: String?
    ) = scope.notClosed {
        sqliteResultCheck(sqlite3_snapshot_open(db, database, snapshot.snapshot))
    }

    override fun recoverSnapshots(database: String?) =
        scope.notClosed { sqliteResultCheck(sqlite3_snapshot_recover(db, database)) }

    override fun tableColumnMetadata(
        table: String,
        column: String,
        database: String?
    ): TableColumnMetadata = scope.notClosed {
        val outDataType = Utf8OutputParam()
        val outCollationSequence = Utf8OutputParam()
        val outNotNull = Int32OutputParam(0)
        val outPrimaryKey = Int32OutputParam(0)
        val outAutoIncrement = Int32OutputParam(0)

        sqliteResultCheck(
            sqlite3_table_column_metadata(
                db = db,
                dbName = database,
                tableName = table,
                columnName = column,
                outDataType = outDataType,
                outCollationSequence = outCollationSequence,
                outNotNull = outNotNull,
                outPrimaryKey = outPrimaryKey,
                outAutoIncrement = outAutoIncrement
            )
        )

        TableColumnMetadataImpl(
            dataType = checkNotNull(outDataType.value),
            collationSequence = checkNotNull(outCollationSequence.value),
            isNullable = outNotNull.value == 0,
            isPrimaryKey = outPrimaryKey.value != 0,
            isAutoIncrement = outAutoIncrement.value != 0,
        )
    }

    override fun setTrace(
        eventCodes: SqliteTraceEventCode?,
        handler: Trace?
    ) = if (eventCodes != null && eventCodes.value != 0) {
        updateHandlerWithResult(
            handler = handler,
            clear = { sqlite3_trace_v2(db, eventCodes, null, null) },
            set = { sqlite3_trace_v2(db, eventCodes, it, TraceCallback) }
        )
    } else {
        // Empty event code simply disable the handler according to SQLite
        scope.notClosed {
            sqliteResultCheck(sqlite3_trace_v2(db, eventCodes, null, null))
        }
    }

    override fun getTransactionState(database: String?): SqliteTransactionState = scope.notClosed {
        sqlite3_txn_state(db, database)
            ?: throwSQLiteException("Database $database is not a valid schema")
    }

    override fun setUpdateHook(handler: UpdateHook?) = updateHandler(
        handler = handler,
        clear = { sqlite3_update_hook(db, null, null) },
        set = { sqlite3_update_hook(db, it, UpdateHookCallback) }
    )

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Listener for connection events.
     */
    interface Listener {

        /**
         * Notifies about statement creation.
         */
        fun onStatementCreated(
            stmt: sqlite3_stmt,
            statement: PreparedStatement
        )

        /**
         * Notifies about statement closing.
         */
        fun onStatementClosed(stmt: sqlite3_stmt)

        /**
         * Notifies about database closing
         */
        fun onConnectionClosed(db: sqlite3)
    }
}