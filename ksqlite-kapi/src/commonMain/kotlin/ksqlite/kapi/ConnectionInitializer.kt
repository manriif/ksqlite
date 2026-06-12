package ksqlite.kapi

import ksqlite.capi.types.Sqlite3TextEncoding
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
        module: VirtualTableModule
    )
}