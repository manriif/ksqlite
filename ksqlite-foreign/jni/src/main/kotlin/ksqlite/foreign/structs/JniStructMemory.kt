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
import ksqlite.foreign.structFree
import ksqlite.structs.Struct
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementation of [Struct.Memory] for JNI, reading from and writing to a direct [ByteBuffer]
 * representing the struct memory.
 */
internal class JniStructMemory(
    override val address: JniPointer,
    buffer: ByteBuffer,
) : Struct.Memory<JniPointer> {

    private val buffer: ByteBuffer = buffer.order(ByteOrder.nativeOrder())

    override fun get(offset: Int): Byte = buffer.get(offset)

    override fun put(offset: Int, value: Byte) {
        buffer.put(offset, value)
    }

    override fun getInt(offset: Int): Int = buffer.getInt(offset)

    override fun putInt(offset: Int, value: Int) {
        buffer.putInt(offset, value)
    }

    override fun getLong(offset: Int): Long = buffer.getLong(offset)

    override fun putLong(offset: Int, value: Long) {
        buffer.putLong(offset, value)
    }

    override fun getDouble(offset: Int): Double = buffer.getDouble(offset)

    override fun putDouble(offset: Int, value: Double) {
        buffer.putDouble(offset, value)
    }

    override fun getPointer(offset: Int): JniPointer = buffer.getLong(offset)

    override fun putPointer(offset: Int, value: JniPointer) {
        buffer.putLong(offset, value)
    }

    override fun close() {
        structFree(buffer)
    }
}