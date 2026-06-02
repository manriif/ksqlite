package ksqlite.capi

import ksqlite.capi.memory.StaticMemoryAllocator
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setPointer
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import ksqlite.ksqlite_xEntryPoint
import java.lang.foreign.MemorySegment

/**
 * Singleton handler for auto extensions.
 */
internal val SharedAutoExtensionHandler = ksqlite_xEntryPoint.allocate({ db, pzErrMsg, pThunk ->
    autoExtensionHandle(
        db = sqlite3(db),
        api = sqlite3_api_routines(pThunk),
        errorPointer = pzErrMsg.takeUnless(MemorySegment::isNull)
    ) { errorPointer, message ->
        errorPointer.setPointer(sqlite3_mprintf(message))
    }
}, StaticMemoryAllocator)