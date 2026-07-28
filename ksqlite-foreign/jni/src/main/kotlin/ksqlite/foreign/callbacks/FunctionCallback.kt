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
package ksqlite.foreign.callbacks

import ksqlite.foreign.JniPointer
import ksqlite.foreign.JniPointerArray

/**
 * Base for function related callback.
 */
public interface FunctionCallback {

    /**
     * Function accepting a single sqlite3_context parameter.
     */
    public interface Func1 : FunctionCallback {

        /**
         * Invoked from JNI.
         */
        public fun apply(context: JniPointer)
    }

    /**
     * Function accepting a sqlite3_context parameter and an array of sqlite3_value.
     */
    public interface Func2 : FunctionCallback {

        /**
         * Invoked from JNI.
         */
        public fun apply(
            context: JniPointer,
            values: JniPointerArray
        )
    }

    /**
     * xFunc function callback used in scalar function.
     */
    public fun interface Func : Func2

    /**
     * xStep function callback used in aggregate and window function.
     */
    public fun interface Step : Func2

    /**
     * xFinal function callback used in aggregate and window function.
     */
    public fun interface Final : Func1

    /**
     * xInverse function callback used in window function.
     */
    public fun interface Inverse : Func2

    /**
     * xValue function callback used in window function.
     */
    public fun interface Value : Func1
}