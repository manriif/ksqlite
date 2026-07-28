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
package ksqlite.capi.memory

/**
 * Base class for all [Struct] implementations.
 */
public abstract class StructBase internal constructor() {

    internal abstract val address: Long

    override fun toString(): String =
        "${this::class.simpleName}(address=0x${address.toHexString(NativeAddressHexFormat)})"
}

/**
 * Represents a native struct.
 *
 * A `Struct` is an opaque handle to a native object identified by its native address.
 * Instances of this class cannot be allocated directly.
 *
 * Two `Struct` instances that refer to the same native object are equal (`==`).
 */
public expect open class Struct : StructBase {

    override val address: Long
}

/**
 * A non-opaque [Struct] that can be closed.
 *
 * Instances created by one of this class's constructors or factory functions are owned by
 * the application.
 *
 * Instances created through [StructArray] factory are owned by the [StructArray].
 *
 * The owner of a [ClosableStruct] is responsible for releasing associated resources.
 *
 * Structs belonging to a [StructArray] must not be closed individually. The whole [StructArray]
 * should be closed instead. An [UnsupportedOperationException] is thrown if a [close] call is made
 * on an individual instance belonging to a [StructArray].
 */
public expect open class ClosableStruct : Struct, AutoCloseable {

    /**
     * Releases the native resources if this struct is owned by the application.
     *
     * If this method is overridden, implementations must call `super.close()`.
     */
    override fun close()
}

/**
 * Owner of a pointer.
 */
internal enum class PointerOwner {

    /**
     * Pointer was allocated and is owned by the application.
     */
    Application,

    /**
     * Pointer is owned by an external entity.
     */
    External,

    /**
     * Pointer was allocated by the application but is owned by a [StructArray].
     */
    InternalArray,
}

/**
 * Handles [ClosableStruct.close], calling [free] when ever the pointer should be closed
 */
internal inline fun PointerOwner.handleClose(free: () -> Unit) = when (this) {
    External -> Unit
    Application -> free()
    InternalArray -> throw UnsupportedOperationException(
        "ClosableStruct belonging to a StructArray cannot be closed"
    )
}