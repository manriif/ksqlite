package ksqlite.capi.handlers

import ksqlite.capi.autoExtensionHandle
import ksqlite.capi.memory.StaticMemoryManager
import ksqlite.capi.memory.isNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import ksqlite.ksqlite_xEntryPoint
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Singleton handler for auto extensions.
 */
internal val SharedAutoExtensionHandler by lazy {
    StaticMemoryManager.functionPointer(::AutoExtensionHandler)
}

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 */
internal class AutoExtensionHandler :
    Handler(),
    ksqlite_xEntryPoint.Function {

    override fun allocate(arena: Arena): MemorySegment =
        ksqlite_xEntryPoint.allocate(this, arena)

    override fun apply(
        db: MemorySegment,
        pzErrMsg: MemorySegment,
        pThunk: MemorySegment
    ): Int = autoExtensionHandle(
        db = sqlite3(db),
        api = sqlite3_api_routines(pThunk),
        errorPointer = pzErrMsg.takeUnless(MemorySegment::isNull)
    ) { errorPointer, message ->
        errorPointer.set(ValueLayout.ADDRESS, 0, sqlite3_mprintf(message))
    }
}