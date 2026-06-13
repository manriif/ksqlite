package ksqlite.kapi.connection

import ksqlite.capi.sqlite3_autovacuum_pages
import ksqlite.capi.sqlite3_busy_handler
import ksqlite.capi.sqlite3_close_v2
import ksqlite.capi.sqlite3_create_function_v2
import ksqlite.capi.sqlite3_create_module_v2
import ksqlite.capi.sqlite3_create_window_function
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.vtab.Sqlite3ModuleVersion
import ksqlite.capi.vtab.callbacks.Sqlite3VTabConnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCreateCallback
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.kapi.blob.Blob
import ksqlite.kapi.callbacks.AutovacuumPages
import ksqlite.kapi.callbacks.BusyHandler
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.callbacks.AutovacuumPagesInvoker
import ksqlite.kapi.callbacks.BusyHandlerInvoker
import ksqlite.kapi.functions.AggregateFunctionFinalInvoker
import ksqlite.kapi.functions.AggregateFunctionStepInvoker
import ksqlite.kapi.functions.ScalarFunctionFuncInvoker
import ksqlite.kapi.functions.WindowFunctionInverseInvoker
import ksqlite.kapi.functions.WindowFunctionValueInvoker
import ksqlite.kapi.helpers.AutoCloser
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.vtab.VTab
import ksqlite.kapi.vtab.VTabBeginInvoker
import ksqlite.kapi.vtab.VTabBestIndexInvoker
import ksqlite.kapi.vtab.VTabCloseInvoker
import ksqlite.kapi.vtab.VTabColumnInvoker
import ksqlite.kapi.vtab.VTabCommitInvoker
import ksqlite.kapi.vtab.VTabConnectInvoker
import ksqlite.kapi.vtab.VTabCreateInvoker
import ksqlite.kapi.vtab.VTabDestroyInvoker
import ksqlite.kapi.vtab.VTabDisconnectInvoker
import ksqlite.kapi.vtab.VTabEofInvoker
import ksqlite.kapi.vtab.VTabFilterInvoker
import ksqlite.kapi.vtab.VTabFindFunctionInvoker
import ksqlite.kapi.vtab.VTabIntegrityInvoker
import ksqlite.kapi.vtab.VTabNextInvoker
import ksqlite.kapi.vtab.VTabOpenInvoker
import ksqlite.kapi.vtab.VTabReleaseInvoker
import ksqlite.kapi.vtab.VTabRenameInvoker
import ksqlite.kapi.vtab.VTabRollbackInvoker
import ksqlite.kapi.vtab.VTabRollbackToInvoker
import ksqlite.kapi.vtab.VTabRowidInvoker
import ksqlite.kapi.vtab.VTabSavepointInvoker
import ksqlite.kapi.vtab.VTabSyncInvoker
import ksqlite.kapi.vtab.VTabUpdateInvoker
import ksqlite.kapi.vtab.VirtualTableModule
import ksqlite.kapi.vtab.VirtualTableOptionalFunction

internal class ConnectionImpl(override val db: sqlite3): Connection() {

    private val modules = mutableListOf<sqlite3_module<*>>()

    override fun openBlob(): Blob {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Functions
    ///////////////////////////////////////////////////////////////////////////

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: ScalarFunction
    ) = db.resultCheck(
        sqlite3_create_function_v2(
            db = db,
            name = name,
            nArg = argumentCount,
            encoding = encoding,
            appData = function,
            func = ScalarFunctionFuncInvoker,
            step = null,
            final = null,
            destroy = AutoCloser
        )
    )

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: AggregateFunction
    ) = db.resultCheck(
        sqlite3_create_function_v2(
            db = db,
            name = name,
            nArg = argumentCount,
            encoding = encoding,
            appData = function,
            func = null,
            step = AggregateFunctionStepInvoker,
            final = AggregateFunctionFinalInvoker,
            destroy = AutoCloser
        )
    )

    override fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: WindowFunction
    ) = db.resultCheck(
        sqlite3_create_window_function(
            db = db,
            name = name,
            nArg = argumentCount,
            encoding = encoding,
            appData = function,
            step = AggregateFunctionStepInvoker,
            final = AggregateFunctionFinalInvoker,
            inverse = WindowFunctionInverseInvoker,
            value = WindowFunctionValueInvoker,
            destroy = AutoCloser
        )
    )

    ///////////////////////////////////////////////////////////////////////////
    // Modules
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Installs a virtual table module.
     */
    private fun <Module : VirtualTableModule> Module.install(
        name: String,
        version: Sqlite3ModuleVersion,
        create: Sqlite3VTabCreateCallback<in Module, VTab>?,
        connect: Sqlite3VTabConnectCallback<in Module, VTab>,
    ) {
        val optionalFunctions = optionalFunctions()

        val sqliteModule = sqlite3_module(
            version = version,
            create = create,
            connect = connect,
            bestIndex = VTabBestIndexInvoker,
            disconnect = VTabDisconnectInvoker,
            destroy = VTabDestroyInvoker,
            open = VTabOpenInvoker,
            close = VTabCloseInvoker,
            filter = VTabFilterInvoker,
            next = VTabNextInvoker,
            eof = VTabEofInvoker,
            column = VTabColumnInvoker,
            rowid = VTabRowidInvoker,
            update = VTabUpdateInvoker
                .takeIf { VirtualTableOptionalFunction.Update in optionalFunctions },
            findFunction = VTabFindFunctionInvoker
                .takeIf { VirtualTableOptionalFunction.FindFunction in optionalFunctions },
            begin = VTabBeginInvoker
                .takeIf { VirtualTableOptionalFunction.Begin in optionalFunctions },
            sync = VTabSyncInvoker
                .takeIf { VirtualTableOptionalFunction.Sync in optionalFunctions },
            commit = VTabCommitInvoker
                .takeIf { VirtualTableOptionalFunction.Commit in optionalFunctions },
            rollback = VTabRollbackInvoker
                .takeIf { VirtualTableOptionalFunction.Rollback in optionalFunctions },
            rename = VTabRenameInvoker
                .takeIf { VirtualTableOptionalFunction.Rename in optionalFunctions },
            savepoint = VTabSavepointInvoker
                .takeIf { VirtualTableOptionalFunction.Savepoint in optionalFunctions },
            release = VTabReleaseInvoker
                .takeIf { VirtualTableOptionalFunction.Release in optionalFunctions },
            rollbackTo = VTabRollbackToInvoker
                .takeIf { VirtualTableOptionalFunction.RollbackTo in optionalFunctions },
            integrity = VTabIntegrityInvoker
                .takeIf { VirtualTableOptionalFunction.Integrity in optionalFunctions },
        )

        val result = sqlite3_create_module_v2(
            db = db,
            name = name,
            module = sqliteModule,
            appData = this,
            destroy = AutoCloser
        )

        if (result == Sqlite3Result.OK) {
            modules.add(sqliteModule)
        } else {
            sqliteModule.close()
            db.resultCheck(result)
        }
    }

    override fun createModule(
        name: String,
        version: Sqlite3ModuleVersion,
        module: VirtualTableModule.Regular
    ) = module.install(
        name = name,
        version = version,
        create = VTabCreateInvoker,
        connect = VTabConnectInvoker
    )

    override fun createModule(
        name: String,
        version: Sqlite3ModuleVersion,
        module: VirtualTableModule.Eponymous
    ) = module.install(
        name = name,
        version = version,
        create = VTabConnectInvoker,
        connect = VTabConnectInvoker
    )

    override fun createModule(
        name: String,
        version: Sqlite3ModuleVersion,
        module: VirtualTableModule.EponymousOnly
    ) = module.install(
        name = name,
        version = version,
        create = null,
        connect = VTabConnectInvoker
    )

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    override fun autoVacuumPages(callback: AutovacuumPages) = sqliteResultCheck(
        sqlite3_autovacuum_pages(db, callback, AutoCloser, AutovacuumPagesInvoker)
    )

    override fun busyHandler(callback: BusyHandler) = sqliteResultCheck(
        sqlite3_busy_handler(db, callback, BusyHandlerInvoker)
    )

    ///////////////////////////////////////////////////////////////////////////
    // Closing
    ///////////////////////////////////////////////////////////////////////////

    override fun close() {
        db.resultCheck(sqlite3_close_v2(db))
        modules.forEach { it.close() }
    }
}