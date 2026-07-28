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
package ksqlite.capi.handlers

import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.memory.toArray
import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_value
import ksqlite.foreign.JniPointer
import ksqlite.foreign.JniPointerArray
import ksqlite.foreign.callbacks.FunctionCallback

/**
 * Base for function [Handler]s.
 */
internal abstract class FunctionHandler : Handler<ApplicationDefinedFunction<*>, Nothing?>() {

    /**
     * Handler for function callback.
     */
    protected inline fun handleFunction(
        context: Long,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handle { function, _ ->
            function.call(sqlite3_context(context))
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 1-arg function [Handler]s.
 */
internal abstract class Function1ArgHandler : FunctionHandler(), FunctionCallback.Func1

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionFinalHandler : Function1ArgHandler(), FunctionCallback.Final {

    override fun apply(context: JniPointer) =
        handleFunction(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionValueHandler : Function1ArgHandler(), FunctionCallback.Value {

    override fun apply(context: JniPointer) =
        handleFunction(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args function [Handler]s.
 */
internal abstract class Function3ArgsHandler : FunctionHandler(), FunctionCallback.Func2 {

    /**
     * Handler for 3-args function callback.
     */
    protected fun handleFunction(
        context: Long,
        values: LongArray,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = handleFunction(context) { context ->
        call(context, values.toArray(::sqlite3_value))
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class FunctionFuncHandler : Function3ArgsHandler(), FunctionCallback.Func {

    override fun apply(
        context: JniPointer,
        values: JniPointerArray
    ) = handleFunction(context, values, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionStepHandler : Function3ArgsHandler(), FunctionCallback.Step {

    override fun apply(
        context: JniPointer,
        values: JniPointerArray
    ) = handleFunction(context, values, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionInverseHandler : Function3ArgsHandler(), FunctionCallback.Inverse {

    override fun apply(
        context: JniPointer,
        values: JniPointerArray
    ) = handleFunction(context, values, ApplicationDefinedFunction<*>::callInverse)
}