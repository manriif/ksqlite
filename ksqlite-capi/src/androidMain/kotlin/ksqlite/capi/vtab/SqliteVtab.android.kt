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

import ksqlite.capi.memory.CloseableStruct
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.types.vtab.SqliteVtab

public actual open class sqlite3_vtab private constructor(private val vTab: s3_vtab) :
    CloseableStruct(vTab, Application),
    MemoryScope,
    SqliteVtab {

    public actual constructor() : this(s3_vtab())

    public actual override val nRef: Int
        get() = vTab.nRef

    public actual override var errMsg: String?
        get() = vTab.zErrMsg.toKStringFromUtf8OrNull()
        set(value) = sqlite3_mprintf(vTab::zErrMsg, value)
}