package ksqlite.capi.handlers

import ksqlite.capi.convertActionCode
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.types.Sqlite3SetAuthorizerCallback
import ksqlite.capi.memory.getStringUtf8OrNull
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class SetAuthorizerHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
    )

    fun handle(
        refPointer: MemorySegment,
        action: Int,
        param3: MemorySegment,
        param4: MemorySegment,
        param5: MemorySegment,
        param6: MemorySegment
    ): Int = handle(refPointer) { callback: Sqlite3SetAuthorizerCallback, userData ->
        callback(
            userData,
            convertActionCode(action),
            param3.getStringUtf8OrNull(),
            param4.getStringUtf8OrNull(),
            param5.getStringUtf8OrNull(),
            param6.getStringUtf8OrNull()
        ).code
    }
}