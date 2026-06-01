package ksqlite.capi.vtab

import ksqlite.capi.createFunction
import ksqlite.capi.functionKey
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.handlers.Handler
import ksqlite.capi.memory.StructPointer
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_module
import ksqlite.sqlite3_module.xCreate
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

internal val VTabCreateHandler: Handler = object : Handler(), xCreate.Function {

    override fun allocate(arena: Arena): MemorySegment = xCreate.allocate(this, arena)

    override fun apply(
        db: MemorySegment,
        refPointer: MemorySegment,
        argc: Int,
        argv: MemorySegment,
        ppVtab: MemorySegment,
        pzErrMsg: MemorySegment
    ): Int = sqlite3(db).vTabConnect(
        module = stableRefData<VTabModule<*, *, *>>(refPointer),
        db = sqlite3(db),
        argv = argv.toStringArrayOrEmpty(argc),
        setVTab = { ppVtab!!.pointed.value = it.pointer },
        setError = { pzErrMsg!!.pointed.value = sqlite3_mprintf(it) }
    )
}