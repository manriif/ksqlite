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

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.stableRefDataHolder
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for upcall stub.
 */
internal abstract class Handler {

    /**
     * Manager owning the data associated to the reference pointer in [handle].
     */
    lateinit var manager: MemoryManager

    /**
     * Allocates a new upcall stub.
     */
    abstract fun allocate(arena: Arena): MemorySegment

    /**
     * Returns [block]'s result, invoked with [Data] and optional appData obtained from a
     * previously referenced [refPointer].
     */
    protected inline fun <reified Data : Any, Result> handle(
        refPointer: MemorySegment,
        block: (data: Data, appData: Any?) -> Result
    ): Result = manager.stableRefDataHolder<Data, Any?>(refPointer).run {
        block(data, appData)
    }
}