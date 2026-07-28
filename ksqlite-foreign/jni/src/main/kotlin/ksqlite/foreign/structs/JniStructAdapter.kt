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
package ksqlite.foreign.structs

import ksqlite.foreign.JniPointer
import ksqlite.foreign.OutputPointer
import ksqlite.foreign.structMalloc
import ksqlite.foreign.structReinterpret
import ksqlite.structs.Struct
import ksqlite.structs.StructType

/**
 * Implementation of [Struct.Adapter] for JNI, mapping pointer to [JniPointer].
 */
internal object JniStructAdapter : Struct.Adapter<JniPointer> {

    override val pointerSize: Int
        get() = JniPointer.SIZE_BYTES

    override val nullPointer: JniPointer
        get() = 0L

    override fun allocate(size: Int): Struct.Memory<JniPointer> {
        val pointer = OutputPointer.OfPointer()
        val buffer = structMalloc(size, pointer)
        return JniStructMemory(pointer.value, buffer)
    }

    override fun reinterpret(
        pointer: JniPointer,
        size: Int
    ): Struct.Memory<JniPointer> = JniStructMemory(pointer, structReinterpret(size, pointer))

    override fun addressAt(pointer: JniPointer, offset: Int): JniPointer = pointer + offset
}