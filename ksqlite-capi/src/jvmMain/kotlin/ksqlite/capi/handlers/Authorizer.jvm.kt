package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback
import ksqlite.capi.convertActionCode
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.`sqlite3_set_authorizer$xAuth`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class AuthorizerHandler :
    Handler(),
    `sqlite3_set_authorizer$xAuth`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_set_authorizer$xAuth`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        action: Int,
        param3: MemorySegment,
        param4: MemorySegment,
        param5: MemorySegment,
        param6: MemorySegment
    ): Int = handle(refPointer) { callback: Sqlite3AuthorizerCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(action),
            param3 = param3.toKStringFromUtf8OrNull(),
            param4 = param4.toKStringFromUtf8OrNull(),
            param5 = param5.toKStringFromUtf8OrNull(),
            param6 = param6.toKStringFromUtf8OrNull()
        ).code
    }
}