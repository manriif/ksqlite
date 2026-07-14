/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_stmt
import ksqlite.foreign.`sqlite3_trace_v2$xCallback`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler :
    Handler(),
    `sqlite3_trace_v2$xCallback`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_trace_v2$xCallback`.allocate(this, arena)

    override fun apply(
        code: Int,
        refPointer: MemorySegment,
        pPointer: MemorySegment,
        xPointer: MemorySegment
    ): Int = handle(refPointer) { callback: SqliteTraceCallback<Any?>, appData ->
        dispatchTraceEvent(
            callback = callback,
            appData = appData,
            code = code,
            pPointer = pPointer,
            xPointer = xPointer,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = MemorySegment::toKStringFromUtf8,
            toLong = { it.get(ValueLayout.JAVA_LONG, 0) }
        )
    }
}