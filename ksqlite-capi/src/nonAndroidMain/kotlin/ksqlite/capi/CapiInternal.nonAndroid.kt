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
package ksqlite.capi

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.destroyMemory
import ksqlite.capi.memory.memory
import ksqlite.types.SqliteResultCode
import ksqlite.types.internal.convertResultCode

/**
 * Invokes [block] which is expected to be the SQLite function that will deallocate [S] and
 * returns [block]'s result.
 *
 * If the deallocation succeeds, which is the case if [block] returns [SqliteResultCode.OK], then
 * all the resources associated with [S] through [memory] are disposed and [memory] is
 * closed before the function returns.
 */
internal inline fun <S> S.deallocate(block: (S) -> Int): SqliteResultCode
        where S : Struct, S : MemoryScope {
    val result = convertResultCode(block(this))

    if (result == SqliteResultCode.OK) {
        destroyMemory()
    }

    return result
}