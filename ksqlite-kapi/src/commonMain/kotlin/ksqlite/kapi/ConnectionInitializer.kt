package ksqlite.kapi

import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.vtab.Sqlite3ModuleVersion
import ksqlite.kapi.callbacks.AutoVacuumPages
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.vtab.VirtualTableModule

public interface ConnectionInitializer {

    public fun autoVacuumPages(autoVacuumPages: AutoVacuumPages)

    public fun createCollation()

    public fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: ScalarFunction
    )

    public fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: AggregateFunction
    )

    public fun createFunction(
        name: String,
        argumentCount: Int,
        encoding: Sqlite3TextEncoding,
        function: WindowFunction
    )

    public fun createModule(
        name: String,
        version: Sqlite3ModuleVersion = Sqlite3ModuleVersion.VERSION_4,
        module: VirtualTableModule.Regular
    )

    public fun createModule(
        name: String,
        version: Sqlite3ModuleVersion = Sqlite3ModuleVersion.VERSION_4,
        module: VirtualTableModule.Eponymous
    )

    public fun createModule(
        name: String,
        version: Sqlite3ModuleVersion = Sqlite3ModuleVersion.VERSION_4,
        module: VirtualTableModule.EponymousOnly
    )
}