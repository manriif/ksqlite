package ksqlite.kapi

import ksqlite.kapi.callbacks.AutoVacuumPages
import ksqlite.kapi.functions.AggregateFunction
import ksqlite.kapi.functions.ScalarFunction
import ksqlite.kapi.functions.WindowFunction
import ksqlite.kapi.vtab.VirtualTableModule

public interface SQLiteConnectionInitializer {

    public fun autoVacuumPages(autoVacuumPages: AutoVacuumPages)

    public fun createCollation()

    public fun createFunction(function: ScalarFunction)

    public fun createFunction(function: AggregateFunction)

    public fun createFunction(function: WindowFunction)

    public fun createModule(module: VirtualTableModule)
}