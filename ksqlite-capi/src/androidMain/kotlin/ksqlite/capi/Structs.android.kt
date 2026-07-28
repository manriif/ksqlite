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

package ksqlite.capi

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.foreign.JniPointer

public actual class sqlite3 internal constructor(pointer: JniPointer) :
    Struct(pointer),
    MemoryScope {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3>() {

        override fun create(pointer: JniPointer): sqlite3 = sqlite3(pointer)
    }
}

public actual class sqlite3_backup internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_blob internal constructor(pointer: JniPointer) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_blob>() {

        override fun create(pointer: JniPointer): sqlite3_blob = sqlite3_blob(pointer)
    }
}

public actual class sqlite3_context internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_filename internal constructor(pointer: JniPointer) :
    Struct(pointer) {

    public actual val content: String
        get() = pointer.toKStringFromUtf8()
}

public actual class sqlite3_snapshot internal constructor(pointer: JniPointer) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_snapshot>() {

        override fun create(pointer: JniPointer): sqlite3_snapshot = sqlite3_snapshot(pointer)
    }
}

public actual class sqlite3_stmt internal constructor(pointer: JniPointer) :
    Struct(pointer),
    MemoryScope {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_stmt>() {

        override fun create(pointer: JniPointer): sqlite3_stmt = sqlite3_stmt(pointer)
    }
}

public actual class sqlite3_value internal constructor(pointer: JniPointer) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_value>() {

        override fun create(pointer: JniPointer): sqlite3_value = sqlite3_value(pointer)
    }
}