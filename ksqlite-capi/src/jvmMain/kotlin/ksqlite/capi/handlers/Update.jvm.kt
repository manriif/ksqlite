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

import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3
import ksqlite.foreign.`sqlite3_preupdate_hook$xPreUpdate`
import ksqlite.foreign.`sqlite3_update_hook$x0`
import ksqlite.types.internal.convertActionCode
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler :
    Handler(),
    `sqlite3_preupdate_hook$xPreUpdate`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_preupdate_hook$xPreUpdate`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        db: MemorySegment,
        action: Int,
        dbName: MemorySegment,
        tableName: MemorySegment,
        iKey1: Long,
        iKey2: Long
    ): Unit = handle(refPointer) { callback: SqlitePreupdateHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            oldRowid = iKey1,
            newRowid = iKey2
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler :
    Handler(),
    `sqlite3_update_hook$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_update_hook$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment,
        action: Int,
        dbName: MemorySegment,
        tableName: MemorySegment,
        rowId: Long
    ): Unit = handle(refPointer) { callback: SqliteUpdateHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            rowid = rowId
        )
    }
}