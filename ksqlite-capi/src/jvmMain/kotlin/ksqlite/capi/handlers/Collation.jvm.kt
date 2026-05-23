package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CreateCollationCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
internal class CollationNeededHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment,
        db: MemorySegment,
        eTextRep: Int,
        name: MemorySegment
    ): Unit = handler(refPointer) { callback: Sqlite3CollationNeededCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name.toKStringFromUtf8()
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CreateCollationHandler(manager: MemoryManager) : Handler(manager) {

    override fun createFunctionDescriptor(): FunctionDescriptor = FunctionDescriptor.of(
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_INT,
        ValueLayout.ADDRESS
    )

    fun handle(
        refPointer: MemorySegment,
        size1: Int,
        text1: MemorySegment,
        size2: Int,
        text2: MemorySegment
    ): Int = handler(refPointer) { callback: Sqlite3CreateCollationCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            left = text1.asSlice(0, size1.toLong()).toKStringFromUtf8(),
            right = text2.asSlice(0, size2.toLong()).toKStringFromUtf8()
        )
    }
}