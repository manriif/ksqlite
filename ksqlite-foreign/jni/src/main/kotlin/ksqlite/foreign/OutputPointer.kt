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
package ksqlite.foreign

/**
 * Output pointer holding a value set on native side.
 */
public sealed class OutputPointer<Value>(initialValue: Value) {

    /**
     * Actual param value.
     */
    public var value: Value = initialValue

    /**
     * 32 bits signed integer output parameter.
     */
    public class OfInt32 @JvmOverloads constructor(initialValue: Int = 0) :
        OutputPointer<Int>(initialValue)

    /**
     * 64 bits signed integer output parameter.
     */
    public class OfInt64 @JvmOverloads constructor(initialValue: Long = 0L) :
        OutputPointer<Long>(initialValue)

    /**
     * 64 bits pointer output parameter.
     */
    public class OfPointer @JvmOverloads constructor(initialValue: Long = 0L) :
        OutputPointer<Long>(initialValue)

    /**
     * String output parameter.
     */
    public class OfString @JvmOverloads constructor(initialValue: String? = null) :
        OutputPointer<String?>(initialValue)

    /**
     * [Value] output parameter.
     */
    public class OfObject<Value> @JvmOverloads constructor(initialValue: Value? = null) :
        OutputPointer<Value?>(initialValue)
}