package ksqlite.capi

import ksqlite.capi.memory.StaticMemoryAllocator
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setPointerValue
import ksqlite.capi.types.sqlite3
import ksqlite.foreign.ksqlite_xEntryPoint
import java.lang.foreign.MemorySegment

/**
 * Singleton handler for auto extensions.
 */
internal val AutoExtensionHandler = ksqlite_xEntryPoint.allocate({ db, pzErrMsg, pThunk ->
    autoExtensionHandle(
        db = sqlite3(db),
        api = pThunk,
        errorPointer = pzErrMsg.takeUnless(MemorySegment::isNull)
    ) { errorPointer, message ->
        errorPointer.setPointerValue(sqlite3_mprintf(message))
    }
}, StaticMemoryAllocator)