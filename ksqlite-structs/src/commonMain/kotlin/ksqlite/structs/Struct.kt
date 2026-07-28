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
package ksqlite.structs

/**
 * Wrapper around a [Memory] pointing to a C struct.
 * Allows reading and writing to the direct memory region where the struct resides.
 */
public abstract class Struct<Type, Member, Pointer : Any> private constructor(
    private val adapter: Adapter<Pointer>,
    private val layout: StructLayout,
    private val isMemoryOwned: Boolean,
    private val memory: Memory<Pointer>
) where Type : StructType<Type, Member>, Member : StructMember<Type> {

    @Suppress("unused")
    private constructor(
        adapter: Adapter<Pointer>,
        layout: StructLayout,
        pointer: Pointer?, // null = allocate, non-null = reinterpret
        size: Int? = null
    ) : this(adapter, layout, pointer == null, run {
        val structSize = layout.structSize

        val allocationSize = size?.also {
            require(it >= structSize) {
                "Allocation size must not be less than the struct layout size"
            }
        } ?: structSize

        if (pointer == null) {
            adapter.allocate(allocationSize)
        } else {
            adapter.reinterpret(pointer, allocationSize)
        }
    })

    internal constructor(
        type: Type,
        adapter: Adapter<Pointer>,
        pointer: Pointer?, // null = allocate, non-null = reinterpret
        size: Int? = null
    ) : this(adapter, type.layout, pointer, size)

    public val pointer: Pointer
        get() = memory.address

    /**
     * Invokes [block] passing it the offset of [member].
     * It is checked that the field length matches [expectedSize].
     *
     * If `null` is returned then allocation may have failed.
     */
    @IgnorableReturnValue
    private inline fun <R> withOffsetAndSize(
        member: Member,
        expectedSize: Int,
        block: Memory<Pointer>.(offset: Int) -> R
    ): R? {
        if (pointer == adapter.nullPointer) {
            // Allocation failed, potentially out of memory
            return null
        }

        val offset = layout.memberOffset(member.ordinal)
        val size = layout.memberSize(member.ordinal)

        check(size == expectedSize) {
            "Trying to access a $size bytes field but $expectedSize bytes were expected"
        }

        return memory.block(offset)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Primitives
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reads [member] value as a single byte.
     */
    protected fun readByte(member: Member): Byte =
        withOffsetAndSize(member, 1, Memory<*>::get) ?: 0

    /**
     * Writes [member] single byte [value].
     */
    protected fun writeByte(member: Member, value: Byte) {
        withOffsetAndSize(member, 1) { put(it, value) }
    }

    /**
     * Reads [member] value as a single unsigned byte.
     */
    protected fun readUByte(member: Member): UByte =
        withOffsetAndSize(member, 1, Memory<*>::get)?.toUByte() ?: 0U

    /**
     * Writes [member] single unsigned byte [value].
     */
    protected fun writeUByte(member: Member, value: UByte) {
        withOffsetAndSize(member, 1) { put(it, value.toByte()) }
    }

    /**
     * Reads [member] value as four bytes.
     */
    protected fun readInt(member: Member): Int =
        withOffsetAndSize(member, 4, Memory<*>::getInt) ?: 0

    /**
     * Writes [member] four bytes [value].
     */
    protected fun writeInt(member: Member, value: Int) {
        withOffsetAndSize(member, 4) { putInt(it, value) }
    }

    /**
     * Reads [member] value as eight bytes.
     */
    protected fun readLong(member: Member): Long =
        withOffsetAndSize(member, 8, Memory<*>::getLong) ?: 0L

    /**
     * Writes [member] eight bytes [value].
     */
    protected fun writeLong(member: Member, value: Long) {
        withOffsetAndSize(member, 8) { putLong(it, value) }
    }

    /**
     * Reads [member] value as eight unsigned bytes.
     */
    protected fun readULong(member: Member): ULong =
        withOffsetAndSize(member, 8, Memory<*>::getLong)?.toULong() ?: 0UL

    /**
     * Writes [member] eight unsigned bytes [value].
     */
    protected fun writeULong(member: Member, value: ULong) {
        withOffsetAndSize(member, 8) { putLong(it, value.toLong()) }
    }

    /**
     * Reads [member] floating value as eight bytes.
     */
    protected fun readDouble(member: Member): Double =
        withOffsetAndSize(member, 8, Memory<*>::getDouble) ?: .0

    /**
     * Writes [member] floating eight bytes [value].
     */
    protected fun writeDouble(member: Member, value: Double) {
        withOffsetAndSize(member, 8) { putDouble(it, value) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Addresses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reads [member] value as a [Pointer].
     */
    protected fun readPointer(member: Member): Pointer = adapter.run {
        withOffsetAndSize(member, pointerSize, Memory<Pointer>::getPointer) ?: nullPointer
    }

    /**
     * Writes [member] pointer [value].
     */
    protected fun writePointer(member: Member, value: Pointer) {
        withOffsetAndSize(member, adapter.pointerSize) { putPointer(it, value) }
    }

    /**
     * Returns the address of the item at index [itemIndex] of the array for which the first item
     * index is located at [baseAddress].
     */
    protected fun <ItemType, Item : Struct<ItemType, *, Pointer>> arrayItem(
        baseAddress: Pointer,
        itemIndex: Int,
        itemType: ItemType,
        factory: (Adapter<Pointer>, Pointer) -> Item
    ): Item where ItemType : StructType<ItemType, *> =
        factory(adapter, adapter.addressAt(baseAddress, itemIndex * itemType.structSize))

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Releases the resources associated with this struct.
     * Throws [IllegalStateException] if the struct was wrapped.
     */
    public open fun free() {
        check(isMemoryOwned) { "Struct is not owned" }
        memory.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Memory
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Memory region covering a whole given struct.
     */
    public interface Memory<Pointer : Any> : AutoCloseable {

        /**
         * Address of the memory region.
         */
        public val address: Pointer

        /**
         * Reads a [Byte] at the given [offset].
         */
        public fun get(offset: Int): Byte

        /**
         * Writes the [value] at the given [offset].
         */
        public fun put(offset: Int, value: Byte)

        /**
         * Reads an [Int] at the given [offset].
         */
        public fun getInt(offset: Int): Int

        /**
         * Writes the [value] at the given [offset].
         */
        public fun putInt(offset: Int, value: Int)

        /**
         * Reads a [Long] at the given [offset].
         */
        public fun getLong(offset: Int): Long

        /**
         * Writes the [value] at the given [offset].
         */
        public fun putLong(offset: Int, value: Long)

        /**
         * Reads a [Double] at the given [offset].
         */
        public fun getDouble(offset: Int): Double

        /**
         * Writes the [value] at the given [offset].
         */
        public fun putDouble(offset: Int, value: Double)

        /**
         * Reads a [Pointer] at the given [offset].
         */
        public fun getPointer(offset: Int): Pointer

        /**
         * Writes the [value] at the given [offset].
         */
        public fun putPointer(offset: Int, value: Pointer)

        /**
         * Frees the memory allocated to the struct.
         * This function is not called if the struct is not owned.
         */
        override fun close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Platform abstraction to native functions to layout and structs.
     */
    public interface Adapter<Pointer : Any> {

        /**
         * Size of a pointer in bytes.
         */
        public val pointerSize: Int

        /**
         * Represents a pointer to `null`.
         */
        public val nullPointer: Pointer

        /**
         * Allocates memory of the given [size] to hold a struct and returns a [Memory] to read
         * from and write to it.
         */
        public fun allocate(size: Int): Memory<Pointer>

        /**
         * Wraps an existing memory identified by [pointer] and returns a [Memory] to read from
         * and write to it.
         */
        public fun reinterpret(pointer: Pointer, size: Int): Memory<Pointer>

        /**
         * Returns the address at the given [offset] relatively to [pointer].
         */
        public fun addressAt(pointer: Pointer, offset: Int): Pointer
    }
}