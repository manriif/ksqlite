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
package ksqlite.foreign.wasm

/**
 * Data type descriptors, sometimes referred to as "IR" (internal representation). These are short
 * strings which identify the specific data types supported by WASM and/or the JS/WASM glue code.
 */
public sealed interface IR {

    public val value: String

    /**
     * Integer [IR]s.
     */
    public sealed interface Integer : IR

    /**
     * Regular JS number.
     */
    public sealed interface Number : Integer

    /**
     * Pointer.
     */
    public data object Ptr : IR {

        override val value: String
            get() = "*"
    }

    /**
     * 8-bit signed integer.
     */
    public data object I8 : Number {

        override val value: String
            get() = "i8"
    }

    /**
     * 16-bit signed integer.
     */
    public data object I16 : Number {

        override val value: String
            get() = "i16"
    }

    /**
     * 32-bit signed integer. Aliases: int, *, ** (noting that * and ** may be remapped dynamically
     * to i64 when WASM environments gain 64-bit pointer capabilities).
     */
    public data object I32 : Number {

        override val value: String
            get() = "i32"
    }

    /**
     * 64-bit signed integer. APIs which use this require that the application has been built with
     * BigInt support, and will throw if that is not the case.
     */
    public data object I64 : Integer {

        override val value: String
            get() = "i64"
    }

    /**
     * 32-bit floating point value. Alias: float.
     */
    public data object F32 : IR {

        override val value: String
            get() = "f32"
    }

    /**
     * 64-bit floating point value. Alias: double.
     */
    public data object F64 : IR {

        override val value: String
            get() = "f64"
    }
}