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

import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.memory.readBytes
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3
import ksqlite.foreign.`sqlite3_collation_needed$x0`
import ksqlite.foreign.`sqlite3_create_collation_v2$xCompare`
import ksqlite.types.internal.convertTextEncoding
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationHandler :
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
    ): Int = handle(refPointer) { callback: SqliteCollationCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            lhs = text1.readBytes(size1),
            rhs = text2.readBytes(size2)
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
    ): Unit = handle(refPointer) { callback: SqliteCollationNeededCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name.toKStringFromUtf8()
        )
    }
}