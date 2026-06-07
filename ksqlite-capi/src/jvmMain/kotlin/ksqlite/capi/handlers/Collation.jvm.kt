package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.`sqlite3_collation_needed$x0`
import ksqlite.`sqlite3_create_collation_v2$xCompare`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationCompareHandler :
    Handler(),
    `sqlite3_create_collation_v2$xCompare`.Function {


    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_collation_v2$xCompare`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        size1: Int,
        text1: MemorySegment,
        size2: Int,
        text2: MemorySegment
    ): Int = handle(refPointer) { callback: Sqlite3CollationCompareCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            lhs = text1.asSlice(0, size1.toLong()).toArray(ValueLayout.JAVA_BYTE),
            rhs = text2.asSlice(0, size2.toLong()).toArray(ValueLayout.JAVA_BYTE)
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
internal class CollationNeededHandler :
    Handler(),
    `sqlite3_collation_needed$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_collation_needed$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        db: MemorySegment,
        eTextRep: Int,
        name: MemorySegment
    ): Unit = handle(refPointer) { callback: Sqlite3CollationNeededCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name.toKStringFromUtf8()
        )
    }
}