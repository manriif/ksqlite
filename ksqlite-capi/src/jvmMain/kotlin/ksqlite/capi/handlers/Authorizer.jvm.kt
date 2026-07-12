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

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.foreign.`sqlite3_set_authorizer$xAuth`
import ksqlite.types.internal.convertActionCode
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
    ): Int = handle(refPointer) { callback: SqliteAuthorizerCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(action),
            detail1 = param3.toKStringFromUtf8OrNull(),
            detail2 = param4.toKStringFromUtf8OrNull(),
            detail3 = param5.toKStringFromUtf8OrNull(),
            detail4 = param6.toKStringFromUtf8OrNull()
        ).code
    }
}