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
 * Base for output parameter.
 */
public interface OutputParam<Value> {

    /**
     * Value written from native side.
     */
    public val value: Value
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

/**
 * Output parameter that accepts an [Int] to be written to.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class Int32OutputParam(initialValue: Int = 0) : OutputParam<Int> {
    override val value: Int
}

/**
 * Output parameter that accepts a [Long] to be written to.
 *
 * An [initialValue] can optionally be supplied.
 */
public expect class Int64OutputParam(initialValue: Long = 0L) : OutputParam<Long> {
    override val value: Long
}

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Output parameter that accepts a UTF-8 encoded [String] to be written to.
 */
public expect class Utf8OutputParam() : OutputParam<String?> {

    /**
     * UTF-8 encoded [String] or `null` if no string has been allocated or allocation failed.
     */
    override val value: String?
}

///////////////////////////////////////////////////////////////////////////
// Transforms
///////////////////////////////////////////////////////////////////////////

/**
 * Output parameter that is backed by an [Int32OutputParam] but the resolved [value] is
 * [transform]ed into [T] when read.
 */
public abstract class Int32TransformOutputParam<T : Any> internal constructor() : OutputParam<T> {

    internal val base = Int32OutputParam()

    override val value: T
        get() = transform(base.value)

    public abstract fun transform(value: Int): T
}

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Throws if [value] is not null.
 *
 * Allowing reuse of pointer based [OutputParam] would require to initialize a pointer to the value
 * it is currently holding. There is currently not such use case.
 */
internal fun ensurePointerInitialValueIsNull(value: Any?) {
    check(value == null) {
        "Pointer based OutputParam cannot be reused"
    }
}