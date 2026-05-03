package ksqlite.capi.handlers

import ksqlite.capi.autoExtensionHandle
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.StaticMemoryManager
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import ksqlite.capi.utils.isNull
import ksqlite.sqlite3.sqlite3_malloc
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Single handler for auto extensions.
 */
internal val SharedAutoExtensionHandler by lazy {
    StaticMemoryManager.functionPointer(::AutoExtensionHandler)
}

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 */
internal class AutoExtensionHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
    )

    fun handle(
        db: MemorySegment,
        pzErrMsg: MemorySegment,
        pApi: MemorySegment
    ): Int = autoExtensionHandle(
        db = sqlite3(db),
        api = sqlite3_api_routines(pApi),
        errorPointer = pzErrMsg.takeUnless(MemorySegment::isNull)
    ) { errorPointer, message ->
        val bytes = message.toByteArray(Charsets.UTF_8)
        val destinationPointer = sqlite3_malloc(bytes.size)

        if (!destinationPointer.isNull) {
            val sourcePointer = MemorySegment.ofArray(bytes)
            MemorySegment.copy(sourcePointer, 0, destinationPointer, 0, bytes.size.toLong())
            errorPointer.set(ValueLayout.ADDRESS, 0, destinationPointer)
        }
    }
}