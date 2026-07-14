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

import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.capi.memory.toNullableStringArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.foreign.`sqlite3_exec$callback`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler :
    Handler(),
    `sqlite3_exec$callback`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_exec$callback`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        columnCount: Int,
        values: MemorySegment,
        names: MemorySegment
    ): Int = handle(refPointer) { callback: SqliteExecCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            columnCount = columnCount,
            columnValues = values.toNullableStringArrayOrEmpty(columnCount),
            columnNames = names.toStringArrayOrEmpty(columnCount)
        )
    }
}