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
@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.foreign.structs

import ksqlite.foreign.js.plus
import ksqlite.foreign.wasm.WasmMemory
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.structs.Struct
import kotlin.js.toDouble
import kotlin.js.toInt
import kotlin.js.toJsBigInt
import kotlin.js.toLong

/**
 * Implementation of [Struct.Memory] for WASM, reading from and writing to [address] which is the
 * struct base address.
 */
internal class WasmStructMemory(
    override val address: WasmPointer,
    private val memory: WasmMemory
) : Struct.Memory<WasmPointer> {

    override fun get(offset: Int): Byte = memory.peek8(address + offset).toInt().toByte()

    override fun put(offset: Int, value: Byte) {
        memory.poke8(address + offset, value)
    }

    override fun getInt(offset: Int): Int = memory.peek32(address + offset).toInt()

    override fun putInt(offset: Int, value: Int) {
        memory.poke32(address + offset, value)
    }

    override fun getLong(offset: Int): Long = memory.peek64(address + offset).toLong()

    override fun putLong(offset: Int, value: Long) {
        memory.poke64(address + offset, value.toJsBigInt())
    }

    override fun getDouble(offset: Int): Double = memory.peek64f(address + offset).toDouble()

    override fun putDouble(offset: Int, value: Double) {
        memory.poke64f(address + offset, value)
    }

    override fun getPointer(offset: Int): WasmPointer = memory.peekPtr(address + offset)

    override fun putPointer(offset: Int, value: WasmPointer) {
        memory.pokePtr(address + offset, value)
    }

    override fun close() {
        memory.dealloc(address)
    }
}