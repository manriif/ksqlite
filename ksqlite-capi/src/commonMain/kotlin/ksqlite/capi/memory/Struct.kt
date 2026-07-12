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
 * A [Struct] that can be closed.
 *
 * Instances created by one of this class's constructors or factory functions are owned by
 * the application.
 *
 * The owner of a `ClosableStruct` is responsible for calling [close].
 */
public expect open class ClosableStruct :
    Struct,
    AutoCloseable {

    /**
     * Releases the native resources if this struct is owned by the application.
     *
     * If this method is overridden, implementations must call `super.close()`.
     */
    override fun close()
}