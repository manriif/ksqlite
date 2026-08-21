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
@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import ksqlite.capi.memory.CloseableStruct
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.destroyMemory
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_mprintf
import ksqlite.types.vtab.SqliteVtab

public actual open class sqlite3_vtab
private constructor(override val pointer: CPointer<s3_vtab>) :
    CloseableStruct(pointer, Application),
    MemoryScope,
    SqliteVtab {

    public actual constructor() : this(allocate())

    public actual override val nRef: Int
        get() = pointer.pointed.nRef

    public actual override var errMsg: String?
        get() = pointer.pointed.zErrMsg?.toKStringFromUtf8()
        set(value) = pointer.pointed.run {
            sqlite3_free(zErrMsg)
            zErrMsg = value?.let { sqlite3_mprintf(it) }
        }

    override fun close() {
        super.close()
        destroyMemory()
    }
}