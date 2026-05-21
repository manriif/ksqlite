package ksqlite.capi.handlers

import ksqlite.capi.autoExtensionHandle
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.StaticMemoryManager
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.isNull
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
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
internal class AutoExtensionHandler(manager: MemoryManager) : Handler<Nothing>(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
    )

    fun handle(
        db: MemorySegment,
        pzErrMsg: MemorySegment,
        pThunk: MemorySegment
    ): Int = autoExtensionHandle(
        db = sqlite3(db),
        api = sqlite3_api_routines(pThunk),
        errorPointer = pzErrMsg.takeUnless(MemorySegment::isNull)
    ) { errorPointer, message ->
        Arena.ofConfined().use { arena ->
            val nativePtr = ksqlite.sqlite3.sqlite3_mprintf
                .makeInvoker()
                .apply(message.allocateUtf8(arena))

            errorPointer.set(ValueLayout.ADDRESS, 0, nativePtr)
        }
    }
}