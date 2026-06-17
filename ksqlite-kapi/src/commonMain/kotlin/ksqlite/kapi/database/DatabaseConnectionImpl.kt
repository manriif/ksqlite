package ksqlite.kapi.database

import co.touchlab.stately.collections.ConcurrentMutableSet
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.sqlite3_autovacuum_pages
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
import ksqlite.capi.sqlite3_db_status64
import ksqlite.capi.sqlite3_deserialize
import ksqlite.capi.sqlite3_drop_modules
import ksqlite.capi.sqlite3_exec
import ksqlite.capi.sqlite3_extended_result_codes
import ksqlite.capi.types.Int64OutputParam
import ksqlite.capi.types.SqliteBlobOutputParam
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.callbacks.SqliteVTabConnectCallback
import ksqlite.capi.vtab.callbacks.SqliteVTabCreateCallback
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.blob.BlobImpl
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.AggregateFunctionFinalCallback
import ksqlite.kapi.functions.AggregateFunctionStepCallback
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.ScalarFunctionFuncCallback
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.functions.WindowFunctionInverseCallback
import ksqlite.kapi.functions.WindowFunctionValueCallback
import ksqlite.kapi.helpers.AutoCloser
import ksqlite.kapi.helpers.DelegatingAtomicCloseableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.helpers.usingParams
import ksqlite.kapi.throwSQLiteException
import ksqlite.kapi.vtab.VTab
import ksqlite.kapi.vtab.VTabBeginCallback
import ksqlite.kapi.vtab.VTabBestIndexCallback
import ksqlite.kapi.vtab.VTabCloseCallback
import ksqlite.kapi.vtab.VTabColumnCallback
import ksqlite.kapi.vtab.VTabCommitCallback
import ksqlite.kapi.vtab.VTabConnectCallback
import ksqlite.kapi.vtab.VTabCreateCallback
import ksqlite.kapi.vtab.VTabDestroyCallback
import ksqlite.kapi.vtab.VTabDisconnectCallback
import ksqlite.kapi.vtab.VTabEofCallback
import ksqlite.kapi.vtab.VTabFilterCallback
import ksqlite.kapi.vtab.VTabFindFunctionCallback
import ksqlite.kapi.vtab.VTabIntegrityCallback
import ksqlite.kapi.vtab.VTabNextCallback
import ksqlite.kapi.vtab.VTabOpenCallback
import ksqlite.kapi.vtab.VTabReleaseCallback
import ksqlite.kapi.vtab.VTabRenameCallback
import ksqlite.kapi.vtab.VTabRollbackCallback
import ksqlite.kapi.vtab.VTabRollbackToCallback
import ksqlite.kapi.vtab.VTabRowidCallback
import ksqlite.kapi.vtab.VTabSavepointCallback
import ksqlite.kapi.vtab.VTabSyncCallback
import ksqlite.kapi.vtab.VTabUpdateCallback
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.kapi.vtab.VirtualTableModuleDestructor
import ksqlite.kapi.vtab.VirtualTableOptionalFunction
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.vtab.SqliteModuleVersion

internal class DatabaseConnectionImpl(
    override val db: sqlite3,
    private val onConnectionClosed: () -> Unit
) : DatabaseConnection() {

    private val scope = DelegatingAtomicCloseableScope(::closeConnection)
    private val closeables = ConcurrentMutableSet<AutoCloseable>()

    override val config = DatabaseConnectionConfigurationImpl(db, scope)
    override val lastError = DatabaseConnectionLastErrorImpl(db, scope)

    override val changes: Long
        get() = scope.notClosed { sqlite3_changes64(db) }

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
        BlobImpl(db, usingParam(SqliteBlobOutputParam()) { outBlob ->
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
        create: SqliteVTabCreateCallback<in Module, VTab>?,
        connect: SqliteVTabConnectCallback<in Module, VTab>,
    ): Unit = scope.notClosed {
        val optionalFunctions = optionalFunctions()

        val sqliteModule = sqlite3_module(
            version = version,
            create = create,
            connect = connect,
            bestIndex = VTabBestIndexCallback,
            disconnect = VTabDisconnectCallback,
            destroy = VTabDestroyCallback,
            open = VTabOpenCallback,
            close = VTabCloseCallback,
            filter = VTabFilterCallback,
            next = VTabNextCallback,
            eof = VTabEofCallback,
            column = VTabColumnCallback,
            rowid = VTabRowidCallback,
            update = VTabUpdateCallback
                .takeIf { VirtualTableOptionalFunction.Update in optionalFunctions },
            findFunction = VTabFindFunctionCallback
                .takeIf { VirtualTableOptionalFunction.FindFunction in optionalFunctions },
            begin = VTabBeginCallback
                .takeIf { VirtualTableOptionalFunction.Begin in optionalFunctions },
            sync = VTabSyncCallback
                .takeIf { VirtualTableOptionalFunction.Sync in optionalFunctions },
            commit = VTabCommitCallback
                .takeIf { VirtualTableOptionalFunction.Commit in optionalFunctions },
            rollback = VTabRollbackCallback
                .takeIf { VirtualTableOptionalFunction.Rollback in optionalFunctions },
            rename = VTabRenameCallback
                .takeIf { VirtualTableOptionalFunction.Rename in optionalFunctions },
            savepoint = VTabSavepointCallback
                .takeIf { VirtualTableOptionalFunction.Savepoint in optionalFunctions },
            release = VTabReleaseCallback
                .takeIf { VirtualTableOptionalFunction.Release in optionalFunctions },
            rollbackTo = VTabRollbackToCallback
                .takeIf { VirtualTableOptionalFunction.RollbackTo in optionalFunctions },
            integrity = VTabIntegrityCallback
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
        create = VTabCreateCallback,
        connect = VTabConnectCallback
    )

    override fun createModule(
        name: String,
        version: SqliteModuleVersion,
        module: VirtualTableModule.Eponymous
    ) = module.install(
        name = name,
        version = version,
        create = VTabConnectCallback,
        connect = VTabConnectCallback
    )

    override fun createModule(
        name: String,
        version: SqliteModuleVersion,
        module: VirtualTableModule.EponymousOnly
    ) = module.install(
        name = name,
        version = version,
        create = null,
        connect = VTabConnectCallback
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

    override fun getFileName(database: String): String? =
        scope.notClosed { sqlite3_db_filename(db, database) }

    override fun getName(index: Int): String? =
        scope.notClosed { sqlite3_db_name(db, index) }

    override fun isReadOnly(database: String): Boolean = scope.notClosed {
        when (sqlite3_db_readonly(db, database)) {
            ReadWrite -> false
            ReadOnly -> true
            UnknownDatabase ->
                throwSQLiteException("No database named $database on this database connection")
        }
    }

    override fun getStatus(
        option: SqliteDbStatusOption,
        reset: Boolean
    ): DatabaseConnectionOptionStatus = scope.notClosed {
        usingParams(
            param1 = Int64OutputParam(-1),
            param2 = Int64OutputParam(-1),
            transform = ::DatabaseConnectionOptionStatus
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
                schema = database,
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

    ///////////////////////////////////////////////////////////////////////////
    // Handler
    ///////////////////////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////////
    // Closing
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Closes the connection and frees resources.
     */
    private fun closeConnection() {
        db.resultCheck(sqlite3_close_v2(db))

        closeables
            .onEach { it.close() }
            .clear()

        onConnectionClosed()
    }

    override fun close() = scope.close()
}